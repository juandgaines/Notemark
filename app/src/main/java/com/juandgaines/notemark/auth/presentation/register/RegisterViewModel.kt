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
            is RegisterAction.OnUsernameChange -> updateUsername(action.username)
            is RegisterAction.OnEmailChange -> updateEmail(action.email)
            is RegisterAction.OnPasswordChange -> updatePassword(action.password)
            is RegisterAction.OnRepeatPasswordChange -> updateRepeatPassword(action.repeatPassword)
            RegisterAction.OnTogglePasswordVisibility -> {
                _state.update { it.copy(isPasswordVisible = !it.isPasswordVisible) }
            }
            RegisterAction.OnToggleRepeatPasswordVisibility -> {
                _state.update { it.copy(isRepeatPasswordVisible = !it.isRepeatPasswordVisible) }
            }
            is RegisterAction.OnUsernameFocusChange -> {
                _state.update {
                    it.copy(
                        isUsernameFocused = action.isFocused,
                        hasUsernameFocusedOnce = it.hasUsernameFocusedOnce || action.isFocused,
                    )
                }
            }
            is RegisterAction.OnEmailFocusChange -> {
                _state.update {
                    it.copy(
                        isEmailFocused = action.isFocused,
                        hasEmailFocusedOnce = it.hasEmailFocusedOnce || action.isFocused,
                    )
                }
            }
            is RegisterAction.OnPasswordFocusChange -> {
                _state.update {
                    it.copy(
                        isPasswordFocused = action.isFocused,
                        hasPasswordFocusedOnce = it.hasPasswordFocusedOnce || action.isFocused,
                    )
                }
            }
            is RegisterAction.OnRepeatPasswordFocusChange -> {
                _state.update {
                    it.copy(
                        isRepeatPasswordFocused = action.isFocused,
                        hasRepeatPasswordFocusedOnce = it.hasRepeatPasswordFocusedOnce || action.isFocused,
                    )
                }
            }
            RegisterAction.OnRegisterClick -> register()
            else -> Unit
        }
    }

    private fun updateUsername(username: String) {
        val isValid = userDataValidator.validateUsername(username)
        _state.update {
            it.copy(
                username = username,
                isUsernameValid = isValid,
                canRegister = computeCanRegister(it.copy(username = username, isUsernameValid = isValid)),
            )
        }
    }

    private fun updateEmail(email: String) {
        val isValid = userDataValidator.validateEmail(email)
        _state.update {
            it.copy(
                email = email,
                isEmailValid = isValid,
                canRegister = computeCanRegister(it.copy(email = email, isEmailValid = isValid)),
            )
        }
    }

    private fun updatePassword(password: String) {
        val isValid = userDataValidator.validatePassword(password)
        val passwordsMatch = password == _state.value.repeatPassword && _state.value.repeatPassword.isNotEmpty()
        _state.update {
            it.copy(
                password = password,
                isPasswordValid = isValid,
                passwordsMatch = passwordsMatch,
                canRegister = computeCanRegister(
                    it.copy(password = password, isPasswordValid = isValid, passwordsMatch = passwordsMatch)
                ),
            )
        }
    }

    private fun updateRepeatPassword(repeatPassword: String) {
        val passwordsMatch = _state.value.password == repeatPassword && repeatPassword.isNotEmpty()
        _state.update {
            it.copy(
                repeatPassword = repeatPassword,
                passwordsMatch = passwordsMatch,
                canRegister = computeCanRegister(it.copy(repeatPassword = repeatPassword, passwordsMatch = passwordsMatch)),
            )
        }
    }

    private fun computeCanRegister(state: RegisterState): Boolean {
        return state.isUsernameValid &&
                state.isEmailValid &&
                state.isPasswordValid &&
                state.passwordsMatch
    }

    private fun register() {
        _state.update { it.copy(isRegistering = true) }
        viewModelScope.launch {
            val registerResult = authRepository.register(
                username = _state.value.username,
                email = _state.value.email,
                password = _state.value.password,
            )

            when (registerResult) {
                is Result.Success -> {
                    val loginResult = authRepository.login(
                        email = _state.value.email,
                        password = _state.value.password,
                    )
                    _state.update { it.copy(isRegistering = false) }

                    when (loginResult) {
                        is Result.Success -> {
                            eventChannel.send(RegisterEvent.RegisterSuccess)
                        }
                        is Result.Failure -> {
                            eventChannel.send(RegisterEvent.RegisterError(loginResult.error.toUiText()))
                        }
                    }
                }
                is Result.Failure -> {
                    _state.update { it.copy(isRegistering = false) }
                    val error = if (registerResult.error == DataError.Remote.CONFLICT) {
                        UiText.StringResource(R.string.email_already_exists)
                    } else {
                        registerResult.error.toUiText()
                    }
                    eventChannel.send(RegisterEvent.RegisterError(error))
                }
            }
        }
    }
}
