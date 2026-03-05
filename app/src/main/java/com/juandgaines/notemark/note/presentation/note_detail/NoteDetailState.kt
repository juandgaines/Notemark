package com.juandgaines.notemark.note.presentation.note_detail

import androidx.compose.foundation.text.input.TextFieldState

data class NoteDetailState(
    val noteId: String = "",
    val titleTextState: TextFieldState = TextFieldState(),
    val contentTextState: TextFieldState = TextFieldState(),
    val isSaving: Boolean = false,
    val hasUnsavedChanges: Boolean = false,
    val showDiscardDialog: Boolean = false,
    val canSave: Boolean = false,
)
