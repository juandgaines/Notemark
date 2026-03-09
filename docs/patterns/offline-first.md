# Offline-First Pattern (Android)

Pattern for building offline-first features where local writes succeed immediately and sync to remote in the background. Uses an application-scoped `CoroutineScope` that survives ViewModel clearing.

## Application Scope Setup

### 1. Add scope to Application class

```kotlin
package com.juandgaines.notemark

import android.app.Application
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob

class App : Application() {

    val applicationScope = CoroutineScope(SupervisorJob())

    override fun onCreate() {
        super.onCreate()
        // ... existing init code
    }
}
```

`SupervisorJob()` ensures a failure in one child coroutine doesn't cancel siblings.

### 2. Provide via Koin

```kotlin
// In appModule
val appModule = module {
    single<CoroutineScope> {
        (androidApplication() as App).applicationScope
    }
    // ... other registrations
}
```

## Why Application Scope?

| Scope | Survives back-navigation? | Survives config change? | Lives until app killed? |
|-------|--------------------------|------------------------|------------------------|
| `viewModelScope` | No | Yes | No |
| `lifecycleScope` | No | No | No |
| `applicationScope` | Yes | Yes | Yes |

Use `applicationScope` when:
- The user triggers a write (save, delete, sync) and navigates away before it completes
- Background sync must finish regardless of which screen is active
- Fire-and-forget remote calls after a successful local write

## Repository Pattern

The core pattern: **write locally first, then sync to remote using applicationScope**.

```kotlin
class ItemRepositoryImpl(
    private val localDataSource: LocalItemDataSource,
    private val remoteDataSource: RemoteItemDataSource,
    private val applicationScope: CoroutineScope,
) : ItemRepository {

    override suspend fun upsertItem(item: Item): EmptyResult<DataError> {
        // 1. Write locally first — this is the source of truth
        val localResult = localDataSource.upsertItem(item)
        if (localResult !is Result.Success) {
            return localResult.asEmptyDataResult()
        }

        val itemWithId = item.copy(id = localResult.data)

        // 2. Sync to remote — survives ViewModel clearing
        applicationScope.launch {
            remoteDataSource.postItem(itemWithId)
                .onSuccess { remoteItem ->
                    // Update local with server response (e.g., server-assigned fields)
                    localDataSource.upsertItem(remoteItem)
                }
                .onFailure {
                    // Remote failed — schedule WorkManager sync for reliable retry
                    syncScheduler.scheduleSync(
                        SyncScheduler.SyncType.CreateItem(itemWithId.id)
                    )
                }
        }

        return Result.Success(Unit)
    }

    override suspend fun deleteItem(id: String): EmptyResult<DataError> {
        // Delete locally first
        localDataSource.deleteItem(id)

        // Fire-and-forget remote delete
        applicationScope.launch {
            remoteDataSource.deleteItem(id)
                .onFailure {
                    // Remote delete failed — schedule WorkManager to retry
                    syncScheduler.scheduleSync(
                        SyncScheduler.SyncType.DeleteItem(id)
                    )
                }
        }

        return Result.Success(Unit)
    }
}
```

> **Note:** Sync scheduling is done inside `applicationScope` so the WorkManager enqueue call completes even if the calling ViewModel or scope is cancelled. This is the standard pattern — the error/failure path is where you schedule reliable sync tasks.

## Parallel Coroutine Execution

When processing multiple independent operations (e.g., syncing N notes, merging remote + local data), run them in parallel using structured concurrency. Choose the scope based on failure tolerance:

### `supervisorScope` — tolerate individual failures

Use when each operation is independent and one failure shouldn't cancel siblings. A failed child doesn't propagate to others.

```kotlin
// Sync multiple notes in parallel — one failure doesn't cancel the rest
suspend fun syncPendingItems() {
    val resolvedOps = resolveOperations(pendingItems)

    supervisorScope {
        resolvedOps.map { (noteId, resolved) ->
            async { syncResolvedOperation(noteId, resolved) }
        }.awaitAll()
    }
}

// Merge remote notes: upsert and cleanup are independent
supervisorScope {
    val upsertJob = launch { upsertRemoteNotes(remoteNotes, localMap, pendingNoteIds) }
    val cleanupJob = launch { removeDeletedNotes(localEntities, remoteNotes, pendingNoteIds) }
    upsertJob.join()
    cleanupJob.join()
}
```

### `coroutineScope` — fail-fast (all or nothing)

Use when operations depend on each other or when any failure means the whole batch should abort.

```kotlin
// Fetch user profile + preferences — both needed, fail if either fails
coroutineScope {
    val profile = async { remoteDataSource.getProfile() }
    val prefs = async { remoteDataSource.getPreferences() }
    saveLocally(profile.await(), prefs.await())
}
```

### Decision guide

| Scenario | Scope | Why |
|----------|-------|-----|
| Sync N independent notes | `supervisorScope` | Note A failing shouldn't prevent Note B from syncing |
| Merge remote + cleanup local | `supervisorScope` | Independent operations, partial success is acceptable |
| Fetch multiple dependent resources | `coroutineScope` | Need all results, one failure invalidates the batch |
| Sequential pipeline (A → B → C) | No scope needed | Just call sequentially with `suspend` functions |

## Key Principles

1. **Local write is the source of truth** — UI updates immediately from local DB
2. **Remote sync is best-effort** — failures don't block the user
3. **applicationScope for all sync work** — any operation that writes data, schedules workers, syncs pending items, or performs logout must use `applicationScope` so it survives ViewModel clearing. Only use `viewModelScope` for UI-only work (observing flows for state updates, navigation events, timers).
4. **Schedule sync on failure** — when a remote call fails, use `applicationScope` to schedule a WorkManager sync task for reliable retry. This ensures the scheduling itself survives scope cancellation.
5. **SupervisorJob** — one failed sync doesn't cancel all others
6. **Observe local data** — UI collects from Room/local Flow, not from remote calls
