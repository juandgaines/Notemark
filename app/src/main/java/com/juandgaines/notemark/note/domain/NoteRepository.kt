package com.juandgaines.notemark.note.domain

import com.juandgaines.notemark.core.domain.util.DataError
import com.juandgaines.notemark.core.domain.util.EmptyResult
import kotlinx.coroutines.flow.Flow

interface NoteRepository {
    fun getNotes(): Flow<List<Note>>
    suspend fun fetchNotes(): EmptyResult<DataError>
    suspend fun createBlankNote(): String
    suspend fun upsertNote(note: Note): EmptyResult<DataError>
    suspend fun deleteNote(noteId: String): EmptyResult<DataError>
    suspend fun getNote(noteId: String): Note?
    suspend fun deleteNoteIfEmpty(noteId: String)
    suspend fun deleteAllNotes()
}
