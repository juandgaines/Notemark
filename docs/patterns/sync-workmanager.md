# Sync with WorkManager (Android)

Pattern for reliable background sync using WorkManager. Combines the offline-first repository pattern with WorkManager for guaranteed delivery of pending operations — even after process death or device restart.

> **This is a structural guide, not a prescriptive rulebook.** Your app's sync strategy may differ based on requirements — conflict resolution (last-write-wins, server-wins, merge), data sensitivity, batch vs individual sync, etc. Adapt the patterns below to fit your specific needs.

## When to Use WorkManager vs applicationScope

| Strategy | Use When |
|----------|----------|
| `applicationScope.launch` | Fire-and-forget sync that can be retried on next app open (e.g., updating an item) |
| **WorkManager OneTimeWorkRequest** | Critical operations that **must** reach the server (e.g., creating/deleting an entity) |
| **WorkManager PeriodicWorkRequest** | Recurring background sync (e.g., fetching new data every 30 min) |

**Rule of thumb:** If the user expects the action to persist even if they kill the app immediately after, use WorkManager.

**Scheduling pattern:** Sync tasks are typically scheduled on the **error/failure path** in the repository — when a remote call fails after a successful local write, use `applicationScope` to schedule the WorkManager job. This ensures the scheduling itself survives ViewModel/scope cancellation. See `offline-first.md` for the full repository pattern.

## Architecture Overview

```
Repository
  ├── Write to local DB (source of truth)
  ├── Record pending operation in a sync table
  └── Schedule WorkManager job
        └── Worker reads pending table → calls remote API → clears pending entry

 ViewModel (first screen after login)
  ├── Schedule periodic fetch (PeriodicWorkRequest)
  └── Retry any pending operations that failed previously
```

## DataError to Worker Result Mapping

Create a utility extension that maps `DataError` to WorkManager's `Result`. This centralizes retry vs failure decisions — transient errors retry, permanent errors fail immediately:

```kotlin
package com.juandgaines.notemark.core.data.sync

import androidx.work.ListenableWorker
import com.juandgaines.notemark.core.domain.util.DataError

fun DataError.toWorkerResult(): ListenableWorker.Result {
    return when (this) {
        DataError.Local.DISK_FULL -> ListenableWorker.Result.failure()
        DataError.Remote.REQUEST_TIMEOUT -> ListenableWorker.Result.retry()
        DataError.Remote.UNAUTHORIZED -> ListenableWorker.Result.retry()
        DataError.Remote.CONFLICT -> ListenableWorker.Result.retry()
        DataError.Remote.TOO_MANY_REQUESTS -> ListenableWorker.Result.retry()
        DataError.Remote.NO_INTERNET -> ListenableWorker.Result.retry()
        DataError.Remote.PAYLOAD_TOO_LARGE -> ListenableWorker.Result.failure()
        DataError.Remote.SERVER_ERROR -> ListenableWorker.Result.retry()
        DataError.Remote.SERIALIZATION -> ListenableWorker.Result.failure()
        DataError.Remote.UNKNOWN -> ListenableWorker.Result.failure()
        else -> ListenableWorker.Result.failure()
    }
}
```

Adjust the variants to match your `DataError` enum — add/remove cases as needed for your API.

## Koin Setup for WorkManager

```kotlin
// In your Application class
import androidx.work.Configuration
import org.koin.androidx.workmanager.koin.workManagerFactory

class App : Application(), Configuration.Provider {

    override fun onCreate() {
        super.onCreate()
        initKoin(this)
    }

    // Required: disable default WorkManager initializer in AndroidManifest.xml
    // and let Koin handle it
    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setMinimumLoggingLevel(android.util.Log.INFO)
            .build()
}
```

```kotlin
// In your Koin modules
import org.koin.androidx.workmanager.dsl.workerOf

val appModule = module {
    // ... existing registrations
    workerOf(::CreateItemWorker)
    workerOf(::DeleteItemWorker)
    workerOf(::FetchItemsWorker)
}
```

**AndroidManifest.xml** — disable default WorkManager initializer:

```xml
<provider
    android:name="androidx.startup.InitializationProvider"
    android:authorities="${applicationId}.androidx-startup"
    android:exported="false"
    tools:node="merge">
    <meta-data
        android:name="androidx.work.WorkManagerInitializer"
        android:authorities="${applicationId}"
        tools:node="remove" />
</provider>
```

## Sync Scheduler Interface

Define a domain-level scheduler interface so the repository doesn't depend on WorkManager directly:

```kotlin
// domain layer
interface SyncScheduler {

    suspend fun scheduleSync(type: SyncType)
    suspend fun cancelAllSyncs()

    sealed interface SyncType {
        data class FetchAll(val interval: kotlin.time.Duration) : SyncType
        data class CreateItem(val itemId: String) : SyncType
        data class DeleteItem(val itemId: String) : SyncType
    }
}
```

## Sync Scheduler Implementation

```kotlin
package com.juandgaines.notemark.core.data.sync

import android.content.Context
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.Data
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.await
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

class WorkManagerSyncScheduler(
    private val context: Context,
    private val pendingSyncDao: PendingSyncDao,
    private val sessionStorage: SessionStorage,
    private val applicationScope: CoroutineScope,
) : SyncScheduler {

    private val workManager = WorkManager.getInstance(context)

    override suspend fun scheduleSync(type: SyncScheduler.SyncType) {
        when (type) {
            is SyncScheduler.SyncType.FetchAll -> scheduleFetchWorker(type.interval)
            is SyncScheduler.SyncType.CreateItem -> scheduleCreateWorker(type.itemId)
            is SyncScheduler.SyncType.DeleteItem -> scheduleDeleteWorker(type.itemId)
        }
    }

    private suspend fun scheduleCreateWorker(itemId: String) {
        val userId = sessionStorage.get()?.userId ?: return
        val entity = PendingCreateSyncEntity(
            itemId = itemId,
            userId = userId,
        )
        pendingSyncDao.upsertPendingCreate(entity)

        val workRequest = OneTimeWorkRequestBuilder<CreateItemWorker>()
            .addTag("create_work")
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()
            )
            .setBackoffCriteria(
                backoffPolicy = BackoffPolicy.EXPONENTIAL,
                backoffDelay = 2000L,
                timeUnit = TimeUnit.MILLISECONDS,
            )
            .setInputData(
                Data.Builder()
                    .putString(CreateItemWorker.ITEM_ID, entity.itemId)
                    .build()
            )
            .build()

        applicationScope.launch {
            workManager.enqueue(workRequest).await()
        }.join()
    }

    private suspend fun scheduleDeleteWorker(itemId: String) {
        val userId = sessionStorage.get()?.userId ?: return
        val entity = PendingDeleteSyncEntity(
            itemId = itemId,
            userId = userId,
        )
        pendingSyncDao.upsertPendingDelete(entity)

        val workRequest = OneTimeWorkRequestBuilder<DeleteItemWorker>()
            .addTag("delete_work")
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()
            )
            .setBackoffCriteria(
                backoffPolicy = BackoffPolicy.EXPONENTIAL,
                backoffDelay = 2000L,
                timeUnit = TimeUnit.MILLISECONDS,
            )
            .setInputData(
                Data.Builder()
                    .putString(DeleteItemWorker.ITEM_ID, entity.itemId)
                    .build()
            )
            .build()

        applicationScope.launch {
            workManager.enqueue(workRequest).await()
        }.join()
    }

    private suspend fun scheduleFetchWorker(interval: kotlin.time.Duration) {
        val isSyncScheduled = withContext(Dispatchers.IO) {
            workManager
                .getWorkInfosByTag("sync_work")
                .get()
                .isNotEmpty()
        }
        if (isSyncScheduled) {
            return
        }

        val workRequest = PeriodicWorkRequestBuilder<FetchItemsWorker>(
            repeatInterval = interval.toJavaDuration()
        )
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()
            )
            .setBackoffCriteria(
                backoffPolicy = BackoffPolicy.EXPONENTIAL,
                backoffDelay = 2000L,
                timeUnit = TimeUnit.MILLISECONDS,
            )
            .setInitialDelay(
                duration = 30,
                timeUnit = TimeUnit.MINUTES,
            )
            .addTag("sync_work")
            .build()

        workManager.enqueue(workRequest).await()
    }

    override suspend fun cancelAllSyncs() {
        WorkManager.getInstance(context)
            .cancelAllWork()
            .await()
    }
}
```

## Worker Examples

### OneTimeWorkRequest — Create Item Worker

```kotlin
class CreateItemWorker(
    context: Context,
    private val params: WorkerParameters,
    private val remoteDataSource: RemoteItemDataSource,
    private val pendingSyncDao: PendingSyncDao,
    private val localDataSource: LocalItemDataSource,
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        if (runAttemptCount >= 5) {
            return Result.failure()
        }

        val itemId = params.inputData.getString(ITEM_ID)
            ?: return Result.failure()

        val pendingEntity = pendingSyncDao.getPendingCreate(itemId)
            ?: return Result.failure()

        val item = localDataSource.getItem(itemId)
            ?: return Result.failure()

        return when (val result = remoteDataSource.createItem(item)) {
            is com.juandgaines.notemark.core.domain.util.Result.Success -> {
                pendingSyncDao.deletePendingCreate(itemId)
                localDataSource.upsertItem(result.data)
                Result.success()
            }
            is com.juandgaines.notemark.core.domain.util.Result.Failure -> {
                result.error.toWorkerResult()
            }
        }
    }

    companion object {
        const val ITEM_ID = "ITEM_ID"
    }
}
```

### OneTimeWorkRequest — Delete Item Worker

```kotlin
class DeleteItemWorker(
    context: Context,
    private val params: WorkerParameters,
    private val remoteDataSource: RemoteItemDataSource,
    private val pendingSyncDao: PendingSyncDao,
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        if (runAttemptCount >= 5) {
            return Result.failure()
        }

        val itemId = params.inputData.getString(ITEM_ID)
            ?: return Result.failure()

        return when (val result = remoteDataSource.deleteItem(itemId)) {
            is com.juandgaines.notemark.core.domain.util.Result.Success -> {
                pendingSyncDao.deletePendingDelete(itemId)
                Result.success()
            }
            is com.juandgaines.notemark.core.domain.util.Result.Failure -> {
                result.error.toWorkerResult()
            }
        }
    }

    companion object {
        const val ITEM_ID = "ITEM_ID"
    }
}
```

### PeriodicWorkRequest — Fetch Items Worker

```kotlin
class FetchItemsWorker(
    context: Context,
    params: WorkerParameters,
    private val itemRepository: ItemRepository,
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        if (runAttemptCount >= 5) {
            return Result.failure()
        }

        return when (val result = itemRepository.fetchItems()) {
            is com.juandgaines.notemark.core.domain.util.Result.Success -> Result.success()
            is com.juandgaines.notemark.core.domain.util.Result.Failure -> result.error.toWorkerResult()
        }
    }
}
```

## Syncing Pending Operations on App Open

When the user opens the app (typically the first screen after login), retry any pending operations that may have failed previously. This complements WorkManager — WorkManager handles retries with backoff, but if the app was force-killed, pending items in the sync table may not have active workers.

### Repository — syncPendingItems()

```kotlin
override suspend fun syncPendingItems() {
    withContext(Dispatchers.IO) {
        val userId = sessionStorage.get()?.userId ?: return@withContext

        val pendingCreates = async {
            pendingSyncDao.getAllPendingCreates(userId)
        }
        val pendingDeletes = async {
            pendingSyncDao.getAllPendingDeletes(userId)
        }

        val createJobs = pendingCreates
            .await()
            .map {
                launch {
                    val item = localDataSource.getItem(it.itemId) ?: return@launch
                    when (remoteDataSource.createItem(item)) {
                        is Result.Failure -> Unit // Will retry on next open or via WorkManager
                        is Result.Success -> {
                            applicationScope.launch {
                                pendingSyncDao.deletePendingCreate(it.itemId)
                            }.join()
                        }
                    }
                }
            }
        val deleteJobs = pendingDeletes
            .await()
            .map {
                launch {
                    when (remoteDataSource.deleteItem(it.itemId)) {
                        is Result.Failure -> Unit
                        is Result.Success -> {
                            applicationScope.launch {
                                pendingSyncDao.deletePendingDelete(it.itemId)
                            }.join()
                        }
                    }
                }
            }

        createJobs.joinAll()
        deleteJobs.joinAll()
    }
}
```

### ViewModel — Initialize Sync on First Screen

In the ViewModel of the first screen shown after login (e.g., the item list), schedule periodic sync and retry pending operations:

```kotlin
private val _state = MutableStateFlow(ItemListState())
val state = _state
    .onStart {
        if (!hasLoadedInitialData) {
            // Schedule periodic background fetch (e.g., every 30 minutes)
            viewModelScope.launch {
                syncScheduler.scheduleSync(
                    type = SyncScheduler.SyncType.FetchAll(30.minutes)
                )
            }

            // Retry any pending create/delete operations from previous sessions
            viewModelScope.launch {
                itemRepository.syncPendingItems()
            }

            // ... load initial data, observe flows, etc.
            hasLoadedInitialData = true
        }
    }
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000L), ItemListState())
```

## Pending Sync Database Entities

```kotlin
@Entity(tableName = "pending_create_sync")
data class PendingCreateSyncEntity(
    @PrimaryKey val itemId: String,
    val userId: String,
)

@Entity(tableName = "pending_delete_sync")
data class PendingDeleteSyncEntity(
    @PrimaryKey val itemId: String,
    val userId: String,
)

@Dao
interface PendingSyncDao {
    @Upsert
    suspend fun upsertPendingCreate(entity: PendingCreateSyncEntity)

    @Query("SELECT * FROM pending_create_sync WHERE itemId = :itemId")
    suspend fun getPendingCreate(itemId: String): PendingCreateSyncEntity?

    @Query("SELECT * FROM pending_create_sync WHERE userId = :userId")
    suspend fun getAllPendingCreates(userId: String): List<PendingCreateSyncEntity>

    @Query("DELETE FROM pending_create_sync WHERE itemId = :itemId")
    suspend fun deletePendingCreate(itemId: String)

    @Upsert
    suspend fun upsertPendingDelete(entity: PendingDeleteSyncEntity)

    @Query("SELECT * FROM pending_delete_sync WHERE itemId = :itemId")
    suspend fun getPendingDelete(itemId: String): PendingDeleteSyncEntity?

    @Query("SELECT * FROM pending_delete_sync WHERE userId = :userId")
    suspend fun getAllPendingDeletes(userId: String): List<PendingDeleteSyncEntity>

    @Query("DELETE FROM pending_delete_sync WHERE itemId = :itemId")
    suspend fun deletePendingDelete(itemId: String)

    @Query("DELETE FROM pending_create_sync")
    suspend fun deleteAllPendingCreates()

    @Query("DELETE FROM pending_delete_sync")
    suspend fun deleteAllPendingDeletes()
}
```

## Choosing Between OneTime and Periodic Tasks

| Scenario | Worker Type | Example |
|----------|-------------|---------|
| User creates/updates an item | `OneTimeWorkRequest` | Sync a new note to the server |
| User deletes an item | `OneTimeWorkRequest` | Delete a note from the server |
| Background data refresh | `PeriodicWorkRequest` | Fetch new notes every 30 minutes |
| Initial sync after login | `OneTimeWorkRequest` | Pull all data from server on first login |

### Decision Checklist

1. **Is it user-initiated?** → `OneTimeWorkRequest` with network constraint
2. **Is it recurring/scheduled?** → `PeriodicWorkRequest` with minimum interval (15 min)
3. **Must it survive process death?** → WorkManager (not applicationScope)
4. **Is it best-effort and can retry on next app open?** → `applicationScope.launch` is sufficient

## Key Principles

1. **Schedule sync on error via applicationScope** — in the repository, when a remote call fails after a local write succeeds, schedule the WorkManager job inside `applicationScope.launch { }`. This ensures the enqueue call completes even if the calling scope is cancelled.
2. **Record before scheduling** — always persist the pending operation to the sync table before enqueuing the WorkManager job
3. **Idempotent workers** — workers may run multiple times; ensure operations are safe to repeat
4. **Network constraints** — always set `NetworkType.CONNECTED` for remote sync workers
5. **Exponential backoff** — use `BackoffPolicy.EXPONENTIAL` with a 2s base delay
6. **Max retry limit** — check `runAttemptCount` and fail after a reasonable number of retries (e.g., 5)
7. **Clean up on success** — delete the pending sync entry only after the remote operation succeeds
8. **Cancel on logout** — call `cancelAllSyncs()` when the user logs out
9. **Use `toWorkerResult()`** — centralize retry vs failure logic in the `DataError.toWorkerResult()` extension instead of hardcoding `Result.retry()` everywhere
10. **Sync on app open** — call `syncPendingItems()` from the first ViewModel to catch operations that slipped through WorkManager retries
11. **Adapt to your strategy** — conflict resolution, merge policies, and sync granularity depend on your app's requirements. This pattern assumes a basic last-write-wins approach; adjust as needed.
