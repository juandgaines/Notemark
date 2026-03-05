package com.juandgaines.notemark.note.data.remote

import com.juandgaines.notemark.core.domain.util.DataError
import com.juandgaines.notemark.core.domain.util.EmptyResult
import com.juandgaines.notemark.core.domain.util.Result
import com.juandgaines.notemark.note.domain.Note

interface RemoteNoteDataSource {
    suspend fun getNotes(): Result<List<Note>, DataError.Remote>
    suspend fun createNote(note: Note): Result<Note, DataError.Remote>
    suspend fun updateNote(note: Note): Result<Note, DataError.Remote>
    suspend fun deleteNote(noteId: String): EmptyResult<DataError.Remote>
}
