package com.juandgaines.notemark.note.presentation.note_detail

import androidx.compose.foundation.text.input.TextFieldState

data class NoteDetailState(
    val noteId: String = "",
    val titleTextState: TextFieldState = TextFieldState(),
    val contentTextState: TextFieldState = TextFieldState(),
    val hasUnsavedChanges: Boolean = false,
    val mode: NoteDetailMode = NoteDetailMode.VIEW,
    val createdAt: String = "",
    val lastEditedAt: String = "",
    val areUiElementsVisible: Boolean = true,
)
