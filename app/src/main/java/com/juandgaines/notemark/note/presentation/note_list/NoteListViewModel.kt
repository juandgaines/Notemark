package com.juandgaines.notemark.note.presentation.note_list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineScope
import com.juandgaines.notemark.core.domain.ConnectivityObserver
import com.juandgaines.notemark.core.domain.SessionStorage
import com.juandgaines.notemark.core.domain.util.onFailure
import com.juandgaines.notemark.core.presentation.util.toUiText
import com.juandgaines.notemark.note.domain.NoteRepository
import com.juandgaines.notemark.note.domain.SyncScheduler
import com.juandgaines.notemark.note.domain.getProfileInitials
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

class NoteListViewModel(
    private val noteRepository: NoteRepository,
    private val sessionStorage: SessionStorage,
    private val connectivityObserver: ConnectivityObserver,
    private val syncScheduler: SyncScheduler,
    private val syncPreferences: SyncPreferences,
    private val applicationScope: CoroutineScope,
) : ViewModel() {

    private var hasLoadedInitialData = false

    private val eventChannel = Channel<NoteListEvent>()
    val events = eventChannel.receiveAsFlow()

    private val _state = MutableStateFlow(NoteListState())
    val state = _state
        .onStart {
            if (!hasLoadedInitialData) {
                loadInitialData()
                hasLoadedInitialData = true
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000L), NoteListState())

    private fun loadInitialData() {
        viewModelScope.launch {
            val authInfo = sessionStorage.get()
            _state.update {
                it.copy(profileInitials = getProfileInitials(authInfo?.username ?: ""))
            }
        }

        viewModelScope.launch {
            noteRepository.getNotes().collect { notes ->
                _state.update { it.copy(notes = notes.map { note -> note.toNoteUi() }) }
            }
        }

        // Observe connectivity
        connectivityObserver.isConnected()
            .onEach { isOnline ->
                _state.update { it.copy(isOffline = !isOnline) }
            }
            .launchIn(viewModelScope)

        // Fetch notes + sync pending items on app open
        _state.update { it.copy(isLoading = true) }
        applicationScope.launch {
            // Retry pending sync items
            noteRepository.syncPendingItems()

            noteRepository.fetchNotes()
                .onFailure { error ->
                    eventChannel.send(NoteListEvent.Error(error.toUiText()))
                }
            _state.update { it.copy(isLoading = false) }
        }

        // Schedule periodic sync based on saved preference
        applicationScope.launch {
            val interval = syncPreferences.getSyncInterval().first()
            if (interval != SyncInterval.MANUAL_ONLY) {
                interval.duration?.let { duration ->
                    syncScheduler.scheduleSync(SyncScheduler.SyncType.FetchAll(duration))
                }
            }
        }
    }

    fun onAction(action: NoteListAction) {
        when (action) {
            is NoteListAction.OnCreateNoteClick -> createNote()
            is NoteListAction.OnNoteLongPress -> {
                _state.update { it.copy(showDeleteDialog = true, noteToDeleteId = action.noteId) }
            }
            is NoteListAction.OnConfirmDelete -> confirmDelete()
            is NoteListAction.OnDismissDeleteDialog -> {
                _state.update { it.copy(showDeleteDialog = false, noteToDeleteId = null) }
            }
            is NoteListAction.OnSettingsClick -> {
                viewModelScope.launch {
                    eventChannel.send(NoteListEvent.NavigateToSettings)
                }
            }
            else -> Unit
        }
    }

    private fun createNote() {
        viewModelScope.launch {
            val noteId = noteRepository.createBlankNote()
            eventChannel.send(NoteListEvent.NavigateToNewNote(noteId))
        }
    }

    private fun confirmDelete() {
        val noteId = _state.value.noteToDeleteId ?: return
        _state.update { it.copy(showDeleteDialog = false, noteToDeleteId = null) }
        viewModelScope.launch {
            noteRepository.deleteNote(noteId)
            eventChannel.send(NoteListEvent.NoteDeleted)
        }
    }
}
