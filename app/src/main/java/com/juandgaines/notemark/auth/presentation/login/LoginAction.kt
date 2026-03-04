package com.juandgaines.notemark.auth.presentation.login

sealed interface LoginAction {
    data class OnEmailChanged(val email: String) : LoginAction
    data class OnPasswordChanged(val password: String) : LoginAction
    data object OnTogglePasswordVisibility : LoginAction
    data object OnLoginClick : LoginAction
    data object OnSignUpClick : LoginAction
}
