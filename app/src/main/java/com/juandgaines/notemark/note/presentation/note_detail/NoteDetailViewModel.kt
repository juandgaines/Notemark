package com.juandgaines.notemark.note.presentation.note_detail

import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.juandgaines.notemark.core.domain.util.onSuccess
import com.juandgaines.notemark.note.domain.Note
import com.juandgaines.notemark.note.domain.NoteRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
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

@OptIn(FlowPreview::class)
class NoteDetailViewModel(
    private val noteRepository: NoteRepository,
    private val applicationScope: CoroutineScope,
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
        val titleFlow = snapshotFlow { _state.value.titleTextState.text.toString() }
            .distinctUntilChanged()

        val contentFlow = snapshotFlow { _state.value.contentTextState.text.toString() }
            .distinctUntilChanged()

        val textChanges = combine(titleFlow, contentFlow) { title, content ->
            Pair(title, content)
        }

        // Update state reactively
        textChanges
            .onEach { (title, content) ->
                _state.update {
                    it.copy(
                        hasUnsavedChanges = title != originalTitle || content != originalContent,
                    )
                }
            }
            .launchIn(viewModelScope)

        // Auto-save with debounce
        textChanges
            .debounce(1_000L)
            .filter { (title, content) ->
                _state.value.mode == NoteDetailMode.EDIT &&
                    title.isNotBlank() &&
                    (title != originalTitle || content != originalContent)
            }
            .onEach { (title, content) -> performAutoSave(title, content) }
            .launchIn(viewModelScope)
    }

    private suspend fun performAutoSave(title: String, content: String) {
        val now = LocalDateTime.now()
        val note = Note(
            id = noteId,
            title = title,
            content = content,
            createdAt = noteCreatedAt,
            lastEditedAt = now,
        )
        noteRepository.upsertNote(note)
            .onSuccess {
                originalTitle = title
                originalContent = content
                _state.update {
                    it.copy(
                        hasUnsavedChanges = false,
                        lastEditedAt = "Just now",
                    )
                }
            }
    }

    fun onAction(action: NoteDetailAction) {
        when (action) {
            is NoteDetailAction.OnCloseClick -> handleClose()
            is NoteDetailAction.OnSwitchToEditMode -> {
                _state.update { it.copy(mode = NoteDetailMode.EDIT) }
            }
            is NoteDetailAction.OnSwitchToViewMode -> {
                val wasReader = _state.value.mode == NoteDetailMode.READER
                _state.update {
                    it.copy(
                        mode = NoteDetailMode.VIEW,
                        areUiElementsVisible = true,
                        lastEditedAt = formatDateTime(LocalDateTime.now()),
                    )
                }
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

    private fun handleClose() {
        val title = _state.value.titleTextState.text.toString()
        val content = _state.value.contentTextState.text.toString()
        val isNewNote = originalTitle.isBlank() && originalContent.isBlank()
        val isEmpty = title.isBlank() && content.isBlank()

        if (isEmpty && isNewNote) {
            applicationScope.launch {
                noteRepository.deleteNoteIfEmpty(noteId)
            }
            viewModelScope.launch {
                eventChannel.send(NoteDetailEvent.CloseScreen)
            }
        } else if (_state.value.hasUnsavedChanges && title.isNotBlank()) {
            // Fire-and-forget save in applicationScope so it survives ViewModel clearing
            val now = LocalDateTime.now()
            val note = Note(
                id = noteId,
                title = title,
                content = content,
                createdAt = noteCreatedAt,
                lastEditedAt = now,
            )
            applicationScope.launch {
                noteRepository.upsertNote(note)
            }
            _state.update {
                it.copy(
                    mode = NoteDetailMode.VIEW,
                    hasUnsavedChanges = false,
                    lastEditedAt = "Just now",
                )
            }
        } else {
            _state.update { it.copy(mode = NoteDetailMode.VIEW) }
        }
    }
}
