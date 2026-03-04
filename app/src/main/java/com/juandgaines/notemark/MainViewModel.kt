package com.juandgaines.notemark

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.juandgaines.notemark.core.domain.SessionStorage
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

class MainViewModel(
    private val sessionStorage: SessionStorage,
) : ViewModel() {

    var state by mutableStateOf(MainState())
        private set

    private val eventChannel = Channel<MainEvent>()
    val events = eventChannel.receiveAsFlow()

    init {
        viewModelScope.launch {
            val authInfo = sessionStorage.get()
            state = state.copy(
                isLoggedIn = authInfo != null,
                isCheckingAuth = false,
            )
        }

        sessionStorage.observe()
            .onEach { authInfo ->
                if (authInfo == null && state.isLoggedIn) {
                    state = state.copy(isLoggedIn = false)
                    eventChannel.send(MainEvent.OnSessionExpired)
                }
            }
            .launchIn(viewModelScope)
    }
}
