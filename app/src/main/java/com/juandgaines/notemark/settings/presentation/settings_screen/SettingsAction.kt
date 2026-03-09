package com.juandgaines.notemark.settings.presentation.settings_screen

import com.juandgaines.notemark.settings.domain.SyncInterval

sealed interface SettingsAction {
    data object OnLogoutClick : SettingsAction
    data object OnBackClick : SettingsAction
    data object OnSyncIntervalClick : SettingsAction
    data class OnSyncIntervalSelected(val interval: SyncInterval) : SettingsAction
    data object OnSyncDataClick : SettingsAction
    data object OnSyncNowClick : SettingsAction
    data object OnLogoutWithoutSyncingClick : SettingsAction
    data object OnDismissUnsyncedDialog : SettingsAction
    data object OnDismissSyncErrorDialog : SettingsAction
}
