package com.juandgaines.notemark.note.presentation.note_detail

import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.juandgaines.notemark.core.domain.util.Result
import com.juandgaines.notemark.core.presentation.util.toUiText
import com.juandgaines.notemark.note.domain.Note
import com.juandgaines.notemark.note.domain.NoteRepository
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDateTime

class NoteDetailViewModel(
    private val noteRepository: NoteRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val noteId: String = checkNotNull(savedStateHandle.get<String>("noteId"))

    private var hasLoadedInitialData = false
    private var originalTitle = ""
    private var originalContent = ""
    private var noteCreatedAt: LocalDateTime = LocalDateTime.now()

    private val eventChannel = Channel<NoteDetailEvent>()
    val events = eventChannel.receiveAsFlow()

    private val _state = MutableStateFlow(NoteDetailState())
    val state = _state
        .onStart {
            if (!hasLoadedInitialData) {
                loadNote()
                observeTextChanges()
                hasLoadedInitialData = true
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000L), NoteDetailState())

    private fun loadNote() {
        viewModelScope.launch {
            val note = noteRepository.getNote(noteId) ?: return@launch
            originalTitle = note.title
            originalContent = note.content
            noteCreatedAt = note.createdAt

            _state.value.titleTextState.setTextAndPlaceCursorAtEnd(note.title)
            _state.value.contentTextState.setTextAndPlaceCursorAtEnd(note.content)

            _state.update {
                it.copy(
                    noteId = note.id,
                    canSave = note.title.isNotBlank(),
                )
            }
        }
    }

    private fun observeTextChanges() {
        snapshotFlow { _state.value.titleTextState.text.toString() }
            .distinctUntilChanged()
            .onEach { title ->
                val content = _state.value.contentTextState.text.toString()
                _state.update {
                    it.copy(
                        hasUnsavedChanges = title != originalTitle || content != originalContent,
                        canSave = title.isNotBlank(),
                    )
                }
            }
            .launchIn(viewModelScope)

        snapshotFlow { _state.value.contentTextState.text.toString() }
            .distinctUntilChanged()
            .onEach { content ->
                val title = _state.value.titleTextState.text.toString()
                _state.update {
                    it.copy(
                        hasUnsavedChanges = title != originalTitle || content != originalContent,
                    )
                }
            }
            .launchIn(viewModelScope)
    }

    fun onAction(action: NoteDetailAction) {
        when (action) {
            is NoteDetailAction.OnSaveClick -> saveNote()
            is NoteDetailAction.OnCloseClick -> handleClose()
            is NoteDetailAction.OnConfirmDiscard -> {
                _state.update { it.copy(showDiscardDialog = false) }
                viewModelScope.launch {
                    noteRepository.deleteNoteIfEmpty(noteId)
                    eventChannel.send(NoteDetailEvent.CloseScreen)
                }
            }
            is NoteDetailAction.OnDismissDiscardDialog -> {
                _state.update { it.copy(showDiscardDialog = false) }
            }
        }
    }

    private fun saveNote() {
        val currentState = _state.value
        if (!currentState.canSave) return

        val title = currentState.titleTextState.text.toString()
        val content = currentState.contentTextState.text.toString()

        viewModelScope.launch {
            _state.update { it.copy(isSaving = true) }
            val note = Note(
                id = noteId,
                title = title,
                content = content,
                createdAt = noteCreatedAt,
                lastEditedAt = LocalDateTime.now(),
            )
            when (val result = noteRepository.upsertNote(note)) {
                is Result.Success -> {
                    originalTitle = title
                    originalContent = content
                    _state.update { it.copy(isSaving = false, hasUnsavedChanges = false) }
                    eventChannel.send(NoteDetailEvent.NoteSaved)
                }
                is Result.Failure -> {
                    _state.update { it.copy(isSaving = false) }
                    eventChannel.send(NoteDetailEvent.Error(result.error.toUiText()))
                }
            }
        }
    }

    private fun handleClose() {
        if (_state.value.hasUnsavedChanges) {
            _state.update { it.copy(showDiscardDialog = true) }
        } else {
            viewModelScope.launch {
                noteRepository.deleteNoteIfEmpty(noteId)
                eventChannel.send(NoteDetailEvent.CloseScreen)
            }
        }
    }
}

