package com.juandgaines.notemark.auth.presentation.register

sealed interface RegisterAction {
    data class OnUsernameChanged(val username: String) : RegisterAction
    data class OnEmailChanged(val email: String) : RegisterAction
    data class OnPasswordChanged(val password: String) : RegisterAction
    data class OnRepeatPasswordChanged(val repeatPassword: String) : RegisterAction
    data object OnUsernameFocusLost : RegisterAction
    data object OnEmailFocusLost : RegisterAction
    data object OnPasswordFocusLost : RegisterAction
    data object OnRepeatPasswordFocusLost : RegisterAction
    data object OnTogglePasswordVisibility : RegisterAction
    data object OnToggleRepeatPasswordVisibility : RegisterAction
    data object OnRegisterClick : RegisterAction
    data object OnLogInClick : RegisterAction
}
