package com.juandgaines.notemark.note.presentation.note_detail

import com.juandgaines.notemark.core.presentation.util.UiText

sealed interface NoteDetailEvent {
    data object NoteSaved : NoteDetailEvent
    data object CloseScreen : NoteDetailEvent
    data class Error(val message: UiText) : NoteDetailEvent
    data object EnterReaderMode : NoteDetailEvent
    data object ExitReaderMode : NoteDetailEvent
}
