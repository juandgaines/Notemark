package com.juandgaines.notemark.note.presentation.note_detail

import androidx.compose.foundation.text.input.TextFieldState
import java.time.LocalDateTime

data class NoteDetailState(
    val noteId: String = "",
    val titleTextState: TextFieldState = TextFieldState(),
    val contentTextState: TextFieldState = TextFieldState(),
    val isSaving: Boolean = false,
    val hasUnsavedChanges: Boolean = false,
    val showDiscardDialog: Boolean = false,
    val canSave: Boolean = false,
    val mode: NoteDetailMode = NoteDetailMode.VIEW,
    val createdAt: LocalDateTime? = null,
    val lastEditedAt: LocalDateTime? = null,
    val isUiVisible: Boolean = true,
)
