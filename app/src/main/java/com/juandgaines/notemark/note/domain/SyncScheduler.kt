package com.juandgaines.notemark.note.domain

import kotlinx.coroutines.flow.Flow
import kotlin.time.Duration

interface SyncScheduler {
    suspend fun scheduleSync(type: SyncType)
    fun observeSyncStatus(tag: String): Flow<SyncStatus>
    suspend fun cancelSync(tag: String)
    suspend fun cancelAllSyncs()

    sealed interface SyncType {
        data class FetchAll(val interval: Duration) : SyncType
        data object SyncNow : SyncType
    }

    enum class SyncStatus { IDLE, RUNNING, SUCCEEDED, FAILED }
}
