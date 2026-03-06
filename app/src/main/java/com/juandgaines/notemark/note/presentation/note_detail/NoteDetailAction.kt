package com.juandgaines.notemark.note.presentation.note_detail

sealed interface NoteDetailAction {
    data object OnSaveClick : NoteDetailAction
    data object OnCloseEditMode : NoteDetailAction
    data object OnConfirmDiscard : NoteDetailAction
    data object OnDismissDiscardDialog : NoteDetailAction
    data object OnBackClick : NoteDetailAction
    data object OnEditClick : NoteDetailAction
    data object OnReaderClick : NoteDetailAction
    data object OnScreenTap : NoteDetailAction
    data object OnScrollStart : NoteDetailAction
    data object OnExitReaderMode : NoteDetailAction
}
