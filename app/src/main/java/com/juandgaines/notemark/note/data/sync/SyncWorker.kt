package com.juandgaines.notemark.note.data.sync

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.juandgaines.notemark.core.domain.SessionStorage
import com.juandgaines.notemark.core.domain.util.onFailure
import com.juandgaines.notemark.core.domain.util.onSuccess
import com.juandgaines.notemark.note.domain.NoteRepository
import com.juandgaines.notemark.settings.domain.SyncPreferences
import timber.log.Timber

class SyncWorker(
    context: Context,
    params: WorkerParameters,
    private val noteRepository: NoteRepository,
    private val sessionStorage: SessionStorage,
    private val syncPreferences: SyncPreferences,
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        if (runAttemptCount >= 5) {
            return Result.failure()
        }

        val authInfo = sessionStorage.get() ?: return Result.failure()
        Timber.d("SyncWorker: starting sync for user ${authInfo.username}")

        return try {
            // 1. Flush pending queue
            noteRepository.syncPendingItems()

            // 2. Fetch remote notes
            var workerResult: Result = Result.success()
            noteRepository.fetchNotes()
                .onSuccess {
                    Timber.d("SyncWorker: sync completed successfully")
                    syncPreferences.setLastSyncTimestamp(System.currentTimeMillis())
                    workerResult = Result.success()
                }
                .onFailure { error ->
                    Timber.e("SyncWorker: fetch failed with $error")
                    workerResult = Result.retry()
                }
            workerResult
        } catch (e: Exception) {
            Timber.e(e, "SyncWorker: unexpected error")
            Result.retry()
        }
    }

    companion object {
        const val SYNC_WORK_TAG = "sync_work"
        const val MANUAL_SYNC_WORK_TAG = "manual_sync_work"
    }
}
