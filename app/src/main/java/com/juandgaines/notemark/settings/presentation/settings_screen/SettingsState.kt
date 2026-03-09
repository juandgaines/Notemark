package com.juandgaines.notemark.settings.presentation.settings_screen

import com.juandgaines.notemark.R
import com.juandgaines.notemark.core.presentation.util.UiText
import com.juandgaines.notemark.settings.domain.SyncInterval

data class SettingsState(
    val isLoggingOut: Boolean = false,
    val syncInterval: SyncInterval = SyncInterval.MANUAL_ONLY,
    val lastSyncTimestamp: UiText = UiText.StringResource(R.string.last_sync_never),
    val isSyncing: Boolean = false,
    val showSyncIntervalDropdown: Boolean = false,
    val isOffline: Boolean = false,
    val showUnsyncedDialog: Boolean = false,
    val showSyncErrorDialog: Boolean = false,
)
