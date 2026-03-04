package com.juandgaines.notemark.auth.presentation.register

data class RegisterState(
    val username: String = "",
    val email: String = "",
    val password: String = "",
    val repeatPassword: String = "",
    val isUsernameValid: Boolean = false,
    val isEmailValid: Boolean = false,
    val isPasswordValid: Boolean = false,
    val passwordsMatch: Boolean = false,
    val isPasswordVisible: Boolean = false,
    val isRepeatPasswordVisible: Boolean = false,
    val isRegistering: Boolean = false,
    val canRegister: Boolean = false,
    val hasUsernameFocusedOnce: Boolean = false,
    val hasEmailFocusedOnce: Boolean = false,
    val hasPasswordFocusedOnce: Boolean = false,
    val hasRepeatPasswordFocusedOnce: Boolean = false,
    val isUsernameFocused: Boolean = false,
    val isEmailFocused: Boolean = false,
    val isPasswordFocused: Boolean = false,
    val isRepeatPasswordFocused: Boolean = false,
)
