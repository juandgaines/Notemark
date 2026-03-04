package com.juandgaines.notemark.auth.presentation.login

import com.juandgaines.notemark.core.presentation.util.UiText

sealed interface LoginEvent {
    data object LoginSuccess : LoginEvent
    data class LoginError(val error: UiText) : LoginEvent
}
