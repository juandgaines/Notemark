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
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
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
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

class NoteDetailViewModel(
    private val noteRepository: NoteRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val noteId: String = checkNotNull(savedStateHandle.get<String>("noteId"))

    private var hasLoadedInitialData = false
    private var originalTitle = ""
    private var originalContent = ""
    private var noteCreatedAt: LocalDateTime = LocalDateTime.now()
    private var autoHideJob: Job? = null

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

            val isNewNote = note.title.isBlank() && note.content.isBlank()
            _state.update {
                it.copy(
                    noteId = note.id,
                    canSave = note.title.isNotBlank(),
                    createdAt = formatDateTime(note.createdAt),
                    lastEditedAt = formatDateTime(note.lastEditedAt),
                    mode = if (isNewNote) NoteDetailMode.EDIT else NoteDetailMode.VIEW,
                )
            }
        }
    }

    private fun formatDateTime(dateTime: LocalDateTime): String {
        val now = LocalDateTime.now()
        val minutesDiff = ChronoUnit.MINUTES.between(dateTime, now)
        return if (minutesDiff < 5) {
            "Just now"
        } else {
            dateTime.format(DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm"))
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
                val isNewNote = originalTitle.isBlank() && originalContent.isBlank()
                if (isNewNote) {
                    viewModelScope.launch {
                        noteRepository.deleteNoteIfEmpty(noteId)
                        eventChannel.send(NoteDetailEvent.CloseScreen)
                    }
                } else {
                    val currentState = _state.value
                    currentState.titleTextState.setTextAndPlaceCursorAtEnd(originalTitle)
                    currentState.contentTextState.setTextAndPlaceCursorAtEnd(originalContent)
                    _state.update { it.copy(hasUnsavedChanges = false, mode = NoteDetailMode.VIEW) }
                }
            }
            is NoteDetailAction.OnDismissDiscardDialog -> {
                _state.update { it.copy(showDiscardDialog = false) }
            }
            is NoteDetailAction.OnSwitchToEditMode -> {
                _state.update { it.copy(mode = NoteDetailMode.EDIT) }
            }
            is NoteDetailAction.OnSwitchToViewMode -> {
                val wasReader = _state.value.mode == NoteDetailMode.READER
                _state.update { it.copy(mode = NoteDetailMode.VIEW, areUiElementsVisible = true) }
                autoHideJob?.cancel()
                if (wasReader) {
                    viewModelScope.launch {
                        eventChannel.send(NoteDetailEvent.ExitReaderMode)
                    }
                }
            }
            is NoteDetailAction.OnSwitchToReaderMode -> {
                _state.update { it.copy(mode = NoteDetailMode.READER, areUiElementsVisible = true) }
                startAutoHideTimer()
                viewModelScope.launch {
                    eventChannel.send(NoteDetailEvent.EnterReaderMode)
                }
            }
            is NoteDetailAction.OnReaderTap -> {
                val currentlyVisible = _state.value.areUiElementsVisible
                if (currentlyVisible) {
                    autoHideJob?.cancel()
                    _state.update { it.copy(areUiElementsVisible = false) }
                } else {
                    _state.update { it.copy(areUiElementsVisible = true) }
                    startAutoHideTimer()
                }
            }
            is NoteDetailAction.OnReaderScroll -> {
                autoHideJob?.cancel()
                _state.update { it.copy(areUiElementsVisible = false) }
            }
            is NoteDetailAction.OnBackClick -> {
                viewModelScope.launch {
                    if (_state.value.mode == NoteDetailMode.READER) {
                        eventChannel.send(NoteDetailEvent.ExitReaderMode)
                    }
                    noteRepository.deleteNoteIfEmpty(noteId)
                    eventChannel.send(NoteDetailEvent.CloseScreen)
                }
            }
        }
    }

    private fun startAutoHideTimer() {
        autoHideJob?.cancel()
        autoHideJob = viewModelScope.launch {
            delay(5_000L)
            _state.update { it.copy(areUiElementsVisible = false) }
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
                    _state.update {
                        it.copy(
                            isSaving = false,
                            hasUnsavedChanges = false,
                            mode = NoteDetailMode.VIEW,
                            lastEditedAt = "Just now",
                        )
                    }
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
        val isNewNote = originalTitle.isBlank() && originalContent.isBlank()
        if (_state.value.hasUnsavedChanges) {
            _state.update { it.copy(showDiscardDialog = true) }
        } else if (isNewNote) {
            viewModelScope.launch {
                noteRepository.deleteNoteIfEmpty(noteId)
                eventChannel.send(NoteDetailEvent.CloseScreen)
            }
        } else {
            _state.update { it.copy(mode = NoteDetailMode.VIEW) }
        }
    }
}
