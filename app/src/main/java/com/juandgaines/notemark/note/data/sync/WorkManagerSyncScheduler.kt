package com.juandgaines.notemark.note.data.sync

import android.content.Context
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.juandgaines.notemark.note.domain.SyncScheduler
import com.juandgaines.notemark.note.domain.SyncScheduler.SyncStatus
import com.juandgaines.notemark.note.domain.SyncScheduler.SyncType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import timber.log.Timber
import java.util.concurrent.TimeUnit

class WorkManagerSyncScheduler(
    context: Context,
) : SyncScheduler {

    private val workManager = WorkManager.getInstance(context)

    private val constraints = Constraints.Builder()
        .setRequiredNetworkType(NetworkType.CONNECTED)
        .build()

    override suspend fun scheduleSync(type: SyncType) {
        when (type) {
            is SyncType.FetchAll -> {
                Timber.d("SyncScheduler: cancelling existing periodic worker and scheduling new one with interval=${type.interval}")
                val request = PeriodicWorkRequestBuilder<SyncWorker>(
                    type.interval.inWholeMinutes,
                    TimeUnit.MINUTES,
                )
                    .setConstraints(constraints)
                    .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 2, TimeUnit.SECONDS)
                    .addTag(SyncWorker.SYNC_WORK_TAG)
                    .build()

                workManager.enqueueUniquePeriodicWork(
                    SyncWorker.SYNC_WORK_TAG,
                    ExistingPeriodicWorkPolicy.CANCEL_AND_REENQUEUE,
                    request,
                )
            }
            is SyncType.SyncNow -> {
                Timber.d("SyncScheduler: scheduling one-time manual sync")
                val request = OneTimeWorkRequestBuilder<SyncWorker>()
                    .setConstraints(constraints)
                    .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 2, TimeUnit.SECONDS)
                    .addTag(SyncWorker.MANUAL_SYNC_WORK_TAG)
                    .build()

                workManager.enqueue(request)
            }
        }
    }

    override fun observeSyncStatus(tag: String): Flow<SyncStatus> {
        return workManager.getWorkInfosByTagFlow(tag).map { workInfos ->
            val latestWorkInfo = workInfos.maxByOrNull { it.id.toString() }
            when (latestWorkInfo?.state) {
                WorkInfo.State.RUNNING -> SyncStatus.RUNNING
                WorkInfo.State.SUCCEEDED -> SyncStatus.SUCCEEDED
                WorkInfo.State.FAILED -> SyncStatus.FAILED
                WorkInfo.State.ENQUEUED, WorkInfo.State.BLOCKED -> SyncStatus.RUNNING
                WorkInfo.State.CANCELLED, null -> SyncStatus.IDLE
            }
        }
    }

    override suspend fun cancelSync(tag: String) {
        Timber.d("SyncScheduler: cancelling all work with tag=$tag")
        workManager.cancelAllWorkByTag(tag)
    }

    override suspend fun cancelAllSyncs() {
        Timber.d("SyncScheduler: cancelling all sync work (logout)")
        workManager.cancelAllWork()
    }
}
