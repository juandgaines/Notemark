package com.juandgaines.notemark.note.data.remote

import com.juandgaines.notemark.core.data.networking.delete
import com.juandgaines.notemark.core.data.networking.get
import com.juandgaines.notemark.core.data.networking.post
import com.juandgaines.notemark.core.data.networking.put
import com.juandgaines.notemark.core.domain.util.DataError
import com.juandgaines.notemark.core.domain.util.EmptyResult
import com.juandgaines.notemark.core.domain.util.Result
import com.juandgaines.notemark.core.domain.util.map
import com.juandgaines.notemark.note.data.dto.CreateNoteRequest
import com.juandgaines.notemark.note.data.dto.NoteDto
import com.juandgaines.notemark.note.data.dto.NoteListResponse
import com.juandgaines.notemark.note.data.dto.UpdateNoteRequest
import com.juandgaines.notemark.note.data.mapper.toNote
import com.juandgaines.notemark.note.domain.Note
import io.ktor.client.HttpClient
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

class KtorRemoteNoteDataSource(
    private val httpClient: HttpClient,
) : RemoteNoteDataSource {

    private val utcFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'")
        .withZone(ZoneOffset.UTC)

    override suspend fun getNotes(): Result<List<Note>, DataError.Remote> {
        return httpClient.get<NoteListResponse>(
            route = "/api/notes",
            queryParameters = mapOf("page" to -1),
        ).map { response -> response.notes.map { it.toNote() } }
    }

    override suspend fun createNote(note: Note): Result<Note, DataError.Remote> {
        return httpClient.post<CreateNoteRequest, NoteDto>(
            route = "/api/notes",
            body = CreateNoteRequest(
                id = note.id,
                title = note.title,
                content = note.content,
                createdAt = note.createdAt.atOffset(ZoneOffset.UTC).format(utcFormatter),
                lastEditedAt = note.lastEditedAt.atOffset(ZoneOffset.UTC).format(utcFormatter),
            ),
        ).map { it.toNote() }
    }

    override suspend fun updateNote(note: Note): Result<Note, DataError.Remote> {
        return httpClient.put<UpdateNoteRequest, NoteDto>(
            route = "/api/notes",
            body = UpdateNoteRequest(
                id = note.id,
                title = note.title,
                content = note.content,
                createdAt = note.createdAt.atOffset(ZoneOffset.UTC).format(utcFormatter),
                lastEditedAt = note.lastEditedAt.atOffset(ZoneOffset.UTC).format(utcFormatter),
            ),
        ).map { it.toNote() }
    }

    override suspend fun deleteNote(noteId: String): EmptyResult<DataError.Remote> {
        return httpClient.delete(route = "/api/notes/$noteId")
    }
}
