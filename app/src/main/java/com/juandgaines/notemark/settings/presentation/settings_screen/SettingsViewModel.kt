package com.juandgaines.notemark.settings.presentation.settings_screen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineScope
import com.juandgaines.notemark.R
import com.juandgaines.notemark.auth.domain.AuthRepository
import com.juandgaines.notemark.core.domain.ConnectivityObserver
import com.juandgaines.notemark.core.domain.SessionStorage
import com.juandgaines.notemark.core.domain.util.onFailure
import com.juandgaines.notemark.core.domain.util.onSuccess
import com.juandgaines.notemark.core.presentation.util.UiText
import com.juandgaines.notemark.core.presentation.util.toUiText
import com.juandgaines.notemark.note.data.sync.SyncWorker
import com.juandgaines.notemark.note.domain.NoteRepository
import com.juandgaines.notemark.note.domain.SyncScheduler
import com.juandgaines.notemark.settings.domain.SyncPreferences
import com.juandgaines.notemark.settings.domain.SyncInterval
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class SettingsViewModel(
    private val authRepository: AuthRepository,
    private val noteRepository: NoteRepository,
    private val syncScheduler: SyncScheduler,
    private val syncPreferences: SyncPreferences,
    private val connectivityObserver: ConnectivityObserver,
    private val sessionStorage: SessionStorage,
    private val applicationScope: CoroutineScope,
) : ViewModel() {

    private var hasLoadedInitialData = false

    private val eventChannel = Channel<SettingsEvent>()
    val events = eventChannel.receiveAsFlow()

    private val _state = MutableStateFlow(SettingsState())
    val state = _state
        .onStart {
            if (!hasLoadedInitialData) {
                loadInitialData()
                hasLoadedInitialData = true
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000L), SettingsState())

    private fun loadInitialData() {
        syncPreferences.getSyncInterval()
            .onEach { interval ->
                _state.update { it.copy(syncInterval = interval) }
            }
            .launchIn(viewModelScope)

        syncPreferences.getLastSyncTimestamp()
            .onEach { timestamp ->
                _state.update { it.copy(lastSyncTimestamp = formatLastSyncTimestamp(timestamp)) }
            }
            .launchIn(viewModelScope)

        connectivityObserver.isConnected()
            .onEach { isOnline ->
                _state.update { it.copy(isOffline = !isOnline) }
            }
            .launchIn(viewModelScope)
    }

    fun onAction(action: SettingsAction) {
        when (action) {
            is SettingsAction.OnLogoutClick -> handleLogoutClick()
            is SettingsAction.OnSyncIntervalClick -> {
                _state.update { it.copy(showSyncIntervalDropdown = !it.showSyncIntervalDropdown) }
            }
            is SettingsAction.OnSyncIntervalSelected -> selectSyncInterval(action.interval)
            is SettingsAction.OnSyncDataClick -> syncData()
            is SettingsAction.OnSyncNowClick -> syncThenLogout()
            is SettingsAction.OnLogoutWithoutSyncingClick -> logoutWithoutSyncing()
            is SettingsAction.OnDismissUnsyncedDialog -> {
                _state.update { it.copy(showUnsyncedDialog = false) }
            }
            is SettingsAction.OnDismissSyncErrorDialog -> {
                _state.update { it.copy(showSyncErrorDialog = false) }
            }
            else -> Unit
        }
    }

    private fun selectSyncInterval(interval: SyncInterval) {
        _state.update { it.copy(showSyncIntervalDropdown = false, syncInterval = interval) }
        applicationScope.launch {
            syncPreferences.setSyncInterval(interval)
            if (interval == SyncInterval.MANUAL_ONLY) {
                syncScheduler.cancelSync(SyncWorker.SYNC_WORK_TAG)
            } else {
                interval.duration?.let { duration ->
                    syncScheduler.scheduleSync(SyncScheduler.SyncType.FetchAll(duration))
                }
            }
        }
    }

    private fun syncData() {
        _state.update { it.copy(isSyncing = true) }
        applicationScope.launch {
            syncScheduler.scheduleSync(SyncScheduler.SyncType.SyncNow)
        }

        // Observe worker status for UI feedback — scoped to ViewModel lifecycle
        syncScheduler.observeSyncStatus(SyncWorker.MANUAL_SYNC_WORK_TAG)
            .onEach { status ->
                when (status) {
                    SyncScheduler.SyncStatus.SUCCEEDED -> {
                        // Timestamp is updated by SyncWorker on success
                        _state.update { it.copy(isSyncing = false) }
                    }
                    SyncScheduler.SyncStatus.FAILED -> {
                        _state.update { it.copy(isSyncing = false) }
                        eventChannel.send(
                            SettingsEvent.Error(UiText.StringResource(R.string.error_sync_failed))
                        )
                    }
                    SyncScheduler.SyncStatus.RUNNING -> Unit
                    SyncScheduler.SyncStatus.IDLE -> Unit
                }
            }
            .launchIn(viewModelScope)
    }

    private fun handleLogoutClick() {
        viewModelScope.launch {
            if (_state.value.isOffline) {
                eventChannel.send(
                    SettingsEvent.Error(UiText.StringResource(R.string.error_offline_logout))
                )
                return@launch
            }

            val userId = sessionStorage.get()?.username ?: ""
            val hasPending = noteRepository.hasPendingSyncs(userId).first()
            if (hasPending) {
                _state.update { it.copy(showUnsyncedDialog = true) }
            } else {
                logout()
            }
        }
    }

    private fun syncThenLogout() {
        _state.update { it.copy(showUnsyncedDialog = false, isSyncing = true) }
        applicationScope.launch {
            try {
                noteRepository.syncPendingItems()
                val userId = sessionStorage.get()?.username ?: ""
                val stillHasPending = noteRepository.hasPendingSyncs(userId).first()
                if (stillHasPending) {
                    _state.update { it.copy(isSyncing = false, showSyncErrorDialog = true) }
                } else {
                    _state.update { it.copy(isSyncing = false) }
                    logout()
                }
            } catch (e: Exception) {
                _state.update { it.copy(isSyncing = false, showSyncErrorDialog = true) }
            }
        }
    }

    private fun logoutWithoutSyncing() {
        _state.update { it.copy(showUnsyncedDialog = false, showSyncErrorDialog = false) }
        applicationScope.launch {
            logout()
        }
    }

    private suspend fun logout() {
        _state.update { it.copy(isLoggingOut = true) }
        syncScheduler.cancelAllSyncs()
        noteRepository.clearAllNotes()
        authRepository.logout()
            .onSuccess {
                eventChannel.send(SettingsEvent.LogoutSuccess)
            }
            .onFailure { error ->
                _state.update { it.copy(isLoggingOut = false) }
                eventChannel.send(SettingsEvent.Error(error.toUiText()))
            }
    }

    private fun formatLastSyncTimestamp(timestamp: Long?): UiText {
        if (timestamp == null) return UiText.StringResource(R.string.last_sync_never)
        val now = System.currentTimeMillis()
        val diffMs = now - timestamp
        val diffMinutes = (diffMs / 60_000).toInt()
        val diffHours = (diffMs / 3_600_000).toInt()

        return when {
            diffMinutes < 1 -> UiText.StringResource(R.string.last_sync_just_now)
            diffMinutes < 60 -> UiText.PluralResource(
                id = R.plurals.last_sync_minutes_ago,
                quantity = diffMinutes,
                args = arrayOf(diffMinutes),
            )
            diffHours < 24 -> UiText.PluralResource(
                id = R.plurals.last_sync_hours_ago,
                quantity = diffHours,
                args = arrayOf(diffHours),
            )
            else -> {
                val instant = Instant.ofEpochMilli(timestamp)
                val dateTime = instant.atZone(ZoneId.systemDefault())
                UiText.DynamicString(
                    dateTime.format(DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm"))
                )
            }
        }
    }
}
