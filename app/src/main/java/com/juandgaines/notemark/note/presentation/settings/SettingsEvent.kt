package com.juandgaines.notemark.note.presentation.settings

import com.juandgaines.notemark.core.presentation.util.UiText

sealed interface SettingsEvent {
    data object LogoutSuccess : SettingsEvent
    data class Error(val message: UiText) : SettingsEvent
}
