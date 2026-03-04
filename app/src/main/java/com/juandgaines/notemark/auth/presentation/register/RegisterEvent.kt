package com.juandgaines.notemark.auth.presentation.register

import com.juandgaines.notemark.core.presentation.util.UiText

sealed interface RegisterEvent {
    data object RegisterSuccess : RegisterEvent
    data class RegisterError(val error: UiText) : RegisterEvent
}
