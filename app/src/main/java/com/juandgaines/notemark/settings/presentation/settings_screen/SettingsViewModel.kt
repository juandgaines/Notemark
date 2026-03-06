package com.juandgaines.notemark.settings.presentation.settings_screen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.juandgaines.notemark.auth.domain.AuthRepository
import com.juandgaines.notemark.core.domain.util.Result
import com.juandgaines.notemark.core.presentation.util.toUiText
import com.juandgaines.notemark.note.domain.NoteRepository
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val authRepository: AuthRepository,
    private val noteRepository: NoteRepository,
) : ViewModel() {

    private val eventChannel = Channel<SettingsEvent>()
    val events = eventChannel.receiveAsFlow()

    private val _state = MutableStateFlow(SettingsState())
    val state = _state.asStateFlow()

    fun onAction(action: SettingsAction) {
        when (action) {
            is SettingsAction.OnLogoutClick -> logout()
            else -> Unit
        }
    }

    private fun logout() {
        viewModelScope.launch {
            _state.update { it.copy(isLoggingOut = true) }
            noteRepository.clearAllNotes()
            when (val result = authRepository.logout()) {
                is Result.Success -> {
                    eventChannel.send(SettingsEvent.LogoutSuccess)
                }
                is Result.Failure -> {
                    _state.update { it.copy(isLoggingOut = false) }
                    eventChannel.send(SettingsEvent.Error(result.error.toUiText()))
                }
            }
        }
    }
}
