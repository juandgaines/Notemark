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
        val remoteResult = remoteDataSource.postItem(itemWithId)

        return when (remoteResult) {
            is Result.Error -> {
                // Remote failed, but local succeeded — still a success from user's perspective
                // TODO: Queue for retry or mark as pending sync
                Result.Success(Unit)
            }
            is Result.Success -> {
                // 3. Update local with server response (e.g., server-assigned fields)
                applicationScope.async {
                    localDataSource.upsertItem(remoteResult.data).asEmptyDataResult()
                }.await()
            }
        }
    }

    override suspend fun deleteItem(id: String): EmptyResult<DataError> {
        // Delete locally first
        localDataSource.deleteItem(id)

        // Fire-and-forget remote delete — no need to await
        applicationScope.launch {
            remoteDataSource.deleteItem(id)
        }

        return Result.Success(Unit)
    }
}
```

## Key Principles

1. **Local write is the source of truth** — UI updates immediately from local DB
2. **Remote sync is best-effort** — failures don't block the user
3. **applicationScope for fire-and-forget** — use `.launch` when you don't need the result, `.async` + `.await()` when you do
4. **SupervisorJob** — one failed sync doesn't cancel all others
5. **Observe local data** — UI collects from Room/local Flow, not from remote calls
