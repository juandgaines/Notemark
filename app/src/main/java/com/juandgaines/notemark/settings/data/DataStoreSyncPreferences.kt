package com.juandgaines.notemark.settings.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.juandgaines.notemark.settings.domain.SyncInterval
import com.juandgaines.notemark.settings.domain.SyncPreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.syncDataStore by preferencesDataStore(name = "sync_preferences")

class DataStoreSyncPreferences(
    private val context: Context,
) : SyncPreferences {

    private val syncIntervalKey = stringPreferencesKey("sync_interval")
    private val lastSyncTimestampKey = stringPreferencesKey("last_sync_timestamp")

    override fun getSyncInterval(): Flow<SyncInterval> {
        return context.syncDataStore.data.map { prefs ->
            val name = prefs[syncIntervalKey] ?: SyncInterval.MANUAL_ONLY.name
            SyncInterval.entries.find { it.name == name } ?: SyncInterval.MANUAL_ONLY
        }
    }

    override suspend fun setSyncInterval(interval: SyncInterval) {
        context.syncDataStore.edit { prefs ->
            prefs[syncIntervalKey] = interval.name
        }
    }

    override fun getLastSyncTimestamp(): Flow<Long?> {
        return context.syncDataStore.data.map { prefs ->
            prefs[lastSyncTimestampKey]?.toLongOrNull()
        }
    }

    override suspend fun setLastSyncTimestamp(timestamp: Long) {
        context.syncDataStore.edit { prefs ->
            prefs[lastSyncTimestampKey] = timestamp.toString()
        }
    }
}
