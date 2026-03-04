package com.juandgaines.notemark.auth.presentation.register

import com.juandgaines.notemark.core.presentation.util.UiText

data class RegisterState(
    val username: String = "",
    val email: String = "",
    val password: String = "",
    val repeatPassword: String = "",
    val usernameError: UiText? = null,
    val emailError: UiText? = null,
    val passwordError: UiText? = null,
    val repeatPasswordError: UiText? = null,
    val isUsernameTouched: Boolean = false,
    val isEmailTouched: Boolean = false,
    val isPasswordTouched: Boolean = false,
    val isRepeatPasswordTouched: Boolean = false,
    val isPasswordVisible: Boolean = false,
    val isRepeatPasswordVisible: Boolean = false,
    val isLoading: Boolean = false,
    val canRegister: Boolean = false,
)
