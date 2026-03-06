package com.juandgaines.notemark.note.presentation.note_detail

sealed interface NoteDetailAction {
    data object OnSaveClick : NoteDetailAction
    data object OnCloseClick : NoteDetailAction
    data object OnConfirmDiscard : NoteDetailAction
    data object OnDismissDiscardDialog : NoteDetailAction
    data object OnSwitchToEditMode : NoteDetailAction
    data object OnSwitchToViewMode : NoteDetailAction
    data object OnSwitchToReaderMode : NoteDetailAction
    data object OnReaderTap : NoteDetailAction
    data object OnReaderScroll : NoteDetailAction
    data object OnBackClick : NoteDetailAction
}
