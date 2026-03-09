package com.juandgaines.notemark.note.presentation.note_list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.juandgaines.notemark.core.domain.SessionStorage
import com.juandgaines.notemark.core.domain.util.Result
import com.juandgaines.notemark.core.presentation.util.toUiText
import com.juandgaines.notemark.note.domain.NoteRepository
import com.juandgaines.notemark.note.domain.getProfileInitials
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class NoteListViewModel(
    private val noteRepository: NoteRepository,
    private val sessionStorage: SessionStorage,
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

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            when (val result = noteRepository.fetchNotes()) {
                is Result.Success -> Unit
                is Result.Failure -> {
                    eventChannel.send(NoteListEvent.Error(result.error.toUiText()))
                }
            }
            _state.update { it.copy(isLoading = false) }
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
