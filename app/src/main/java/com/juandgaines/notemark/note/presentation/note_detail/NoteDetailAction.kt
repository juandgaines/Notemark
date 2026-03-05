package com.juandgaines.notemark.note.presentation.note_detail

sealed interface NoteDetailAction {
    data object OnSaveClick : NoteDetailAction
    data object OnCloseClick : NoteDetailAction
    data object OnConfirmDiscard : NoteDetailAction
    data object OnDismissDiscardDialog : NoteDetailAction
}
