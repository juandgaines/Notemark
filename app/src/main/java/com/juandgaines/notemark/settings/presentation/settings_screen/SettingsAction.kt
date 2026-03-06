package com.juandgaines.notemark.settings.presentation.settings_screen

sealed interface SettingsAction {
    data object OnLogoutClick : SettingsAction
    data object OnBackClick : SettingsAction
}
