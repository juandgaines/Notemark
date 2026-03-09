package com.juandgaines.notemark.settings.presentation.settings_screen

import com.juandgaines.notemark.core.presentation.util.UiText

sealed interface SettingsEvent {
    data object LogoutSuccess : SettingsEvent
    data class Error(val message: UiText) : SettingsEvent
}
