package com.juandgaines.notemark.settings.domain

import kotlinx.coroutines.flow.Flow

interface SyncPreferences {
    fun getSyncInterval(): Flow<SyncInterval>
    suspend fun setSyncInterval(interval: SyncInterval)
    fun getLastSyncTimestamp(): Flow<Long?>
    suspend fun setLastSyncTimestamp(timestamp: Long)
}
