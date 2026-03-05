package com.juandgaines.notemark.note.presentation.note_list

data class NoteListState(
    val notes: List<NoteUi> = emptyList(),
    val isLoading: Boolean = false,
    val profileInitials: String = "",
    val showDeleteDialog: Boolean = false,
    val noteToDeleteId: String? = null,
)
