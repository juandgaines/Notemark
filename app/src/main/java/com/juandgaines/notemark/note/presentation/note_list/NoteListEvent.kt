package com.juandgaines.notemark.note.presentation.note_list

import com.juandgaines.notemark.core.presentation.util.UiText

sealed interface NoteListEvent {
    data class Error(val message: UiText) : NoteListEvent
    data object NoteDeleted : NoteListEvent
    data class NavigateToNewNote(val noteId: String) : NoteListEvent
}
