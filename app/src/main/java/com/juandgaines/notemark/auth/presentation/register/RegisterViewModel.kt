package com.juandgaines.notemark.auth.presentation.register

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.juandgaines.notemark.R
import com.juandgaines.notemark.auth.domain.AuthRepository
import com.juandgaines.notemark.auth.domain.UserDataValidator
import com.juandgaines.notemark.core.domain.util.DataError
import com.juandgaines.notemark.core.domain.util.Result
import com.juandgaines.notemark.core.presentation.util.UiText
import com.juandgaines.notemark.core.presentation.util.toUiText
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class RegisterViewModel(
    private val authRepository: AuthRepository,
    private val userDataValidator: UserDataValidator,
) : ViewModel() {

    private val eventChannel = Channel<RegisterEvent>()
    val events = eventChannel.receiveAsFlow()

    private val _state = MutableStateFlow(RegisterState())
    val state = _state
        .onStart { }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000L), RegisterState())

    fun onAction(action: RegisterAction) {
        when (action) {
            is RegisterAction.OnUsernameChanged -> {
                _state.update { it.copy(username = action.username).updateCanRegister() }
            }
            is RegisterAction.OnEmailChanged -> {
                _state.update { it.copy(email = action.email).updateCanRegister() }
            }
            is RegisterAction.OnPasswordChanged -> {
                _state.update { it.copy(password = action.password).updateCanRegister() }
            }
            is RegisterAction.OnRepeatPasswordChanged -> {
                _state.update { it.copy(repeatPassword = action.repeatPassword).updateCanRegister() }
            }
            RegisterAction.OnUsernameFocusLost -> {
                _state.update {
                    it.copy(
                        isUsernameTouched = true,
                        usernameError = validateUsername(it.username),
                    )
                }
            }
            RegisterAction.OnEmailFocusLost -> {
                _state.update {
                    it.copy(
                        isEmailTouched = true,
                        emailError = validateEmail(it.email),
                    )
                }
            }
            RegisterAction.OnPasswordFocusLost -> {
                _state.update {
                    it.copy(
                        isPasswordTouched = true,
                        passwordError = validatePassword(it.password),
                    )
                }
            }
            RegisterAction.OnRepeatPasswordFocusLost -> {
                _state.update {
                    it.copy(
                        isRepeatPasswordTouched = true,
                        repeatPasswordError = validateRepeatPassword(it.password, it.repeatPassword),
                    )
                }
            }
            RegisterAction.OnTogglePasswordVisibility -> {
                _state.update { it.copy(isPasswordVisible = !it.isPasswordVisible) }
            }
            RegisterAction.OnToggleRepeatPasswordVisibility -> {
                _state.update { it.copy(isRepeatPasswordVisible = !it.isRepeatPasswordVisible) }
            }
            RegisterAction.OnRegisterClick -> register()
            RegisterAction.OnLogInClick -> Unit
        }
    }

    private fun register() {
        val currentState = _state.value
        _state.update { it.copy(isLoading = true) }

        viewModelScope.launch {
            val result = authRepository.register(
                username = currentState.username,
                email = currentState.email,
                password = currentState.password,
            )
            _state.update { it.copy(isLoading = false) }

            when (result) {
                is Result.Success -> {
                    eventChannel.send(RegisterEvent.RegisterSuccess)
                }
                is Result.Failure -> {
                    val error = if (result.error == DataError.Remote.CONFLICT) {
                        UiText.StringResource(R.string.error_email_already_used)
                    } else {
                        result.error.toUiText()
                    }
                    eventChannel.send(RegisterEvent.RegisterError(error))
                }
            }
        }
    }

    private fun validateUsername(username: String): UiText? {
        return when {
            username.length < 3 -> UiText.StringResource(R.string.error_username_too_short)
            username.length > 20 -> UiText.StringResource(R.string.error_username_too_long)
            else -> null
        }
    }

    private fun validateEmail(email: String): UiText? {
        return if (!userDataValidator.validateEmail(email)) {
            UiText.StringResource(R.string.error_invalid_email)
        } else null
    }

    private fun validatePassword(password: String): UiText? {
        return if (!userDataValidator.validatePassword(password)) {
            UiText.StringResource(R.string.error_invalid_password)
        } else null
    }

    private fun validateRepeatPassword(password: String, repeatPassword: String): UiText? {
        return if (!userDataValidator.validatePasswordsMatch(password, repeatPassword)) {
            UiText.StringResource(R.string.error_passwords_dont_match)
        } else null
    }

    private fun RegisterState.updateCanRegister(): RegisterState {
        return copy(
            canRegister = userDataValidator.validateUsername(username) &&
                userDataValidator.validateEmail(email) &&
                userDataValidator.validatePassword(password) &&
                userDataValidator.validatePasswordsMatch(password, repeatPassword)
        )
    }
}
