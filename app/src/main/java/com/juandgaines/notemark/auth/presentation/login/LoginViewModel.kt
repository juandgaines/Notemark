package com.juandgaines.notemark.auth.presentation.login

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

class LoginViewModel(
    private val authRepository: AuthRepository,
    private val userDataValidator: UserDataValidator,
) : ViewModel() {

    private val eventChannel = Channel<LoginEvent>()
    val events = eventChannel.receiveAsFlow()

    private val _state = MutableStateFlow(LoginState())
    val state = _state
        .onStart { }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000L), LoginState())

    fun onAction(action: LoginAction) {
        when (action) {
            is LoginAction.OnEmailChange -> {
                val isValid = userDataValidator.validateEmail(action.email)
                _state.update {
                    it.copy(
                        email = action.email,
                        isEmailValid = isValid,
                        canLogin = isValid && it.password.isNotEmpty(),
                    )
                }
            }
            is LoginAction.OnPasswordChange -> {
                _state.update {
                    it.copy(
                        password = action.password,
                        canLogin = it.isEmailValid && action.password.isNotEmpty(),
                    )
                }
            }
            LoginAction.OnTogglePasswordVisibility -> {
                _state.update { it.copy(isPasswordVisible = !it.isPasswordVisible) }
            }
            LoginAction.OnLoginClick -> login()
            else -> Unit
        }
    }

    private fun login() {
        _state.update { it.copy(isLoggingIn = true) }
        viewModelScope.launch {
            val result = authRepository.login(
                email = _state.value.email,
                password = _state.value.password,
            )
            _state.update { it.copy(isLoggingIn = false) }

            when (result) {
                is Result.Success -> {
                    eventChannel.send(LoginEvent.LoginSuccess)
                }
                is Result.Failure -> {
                    val error = if (result.error == DataError.Remote.UNAUTHORIZED) {
                        UiText.StringResource(R.string.invalid_login_credentials)
                    } else {
                        result.error.toUiText()
                    }
                    eventChannel.send(LoginEvent.LoginError(error))
                }
            }
        }
    }
}
