package com.juandgaines.notemark.note.presentation.note_list

sealed interface NoteListAction {
    data class OnNoteClick(val noteId: String) : NoteListAction
    data class OnNoteLongPress(val noteId: String) : NoteListAction
    data object OnCreateNoteClick : NoteListAction
    data object OnConfirmDelete : NoteListAction
    data object OnDismissDeleteDialog : NoteListAction
}
