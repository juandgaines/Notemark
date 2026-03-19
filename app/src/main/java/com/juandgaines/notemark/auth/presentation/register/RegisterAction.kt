package com.juandgaines.notemark.auth.presentation.register

sealed interface RegisterAction {
    data class OnUsernameChange(val username: String) : RegisterAction
    data class OnEmailChange(val email: String) : RegisterAction
    data class OnPasswordChange(val password: String) : RegisterAction
    data class OnRepeatPasswordChange(val repeatPassword: String) : RegisterAction
    data object OnTogglePasswordVisibility : RegisterAction
    data object OnToggleRepeatPasswordVisibility : RegisterAction
    data class OnUsernameFocusChange(val isFocused: Boolean) : RegisterAction
    data class OnEmailFocusChange(val isFocused: Boolean) : RegisterAction
    data class OnPasswordFocusChange(val isFocused: Boolean) : RegisterAction
    data class OnRepeatPasswordFocusChange(val isFocused: Boolean) : RegisterAction
    data object OnRegisterClick : RegisterAction
    data object OnLoginClick : RegisterAction
}
