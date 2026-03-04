package com.juandgaines.notemark.auth.presentation.login

data class LoginState(
    val email: String = "",
    val password: String = "",
    val isEmailValid: Boolean = false,
    val isPasswordVisible: Boolean = false,
    val isLoggingIn: Boolean = false,
    val canLogin: Boolean = false,
)
