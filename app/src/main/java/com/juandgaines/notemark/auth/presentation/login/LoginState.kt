package com.juandgaines.notemark.auth.presentation.login

data class LoginState(
    val email: String = "",
    val password: String = "",
    val isPasswordVisible: Boolean = false,
    val isLoading: Boolean = false,
    val canLogin: Boolean = false,
)
