package com.juandgaines.notemark.note.data

import com.juandgaines.notemark.core.domain.util.DataError
import com.juandgaines.notemark.core.domain.util.EmptyResult
import com.juandgaines.notemark.core.domain.util.Result
import com.juandgaines.notemark.note.data.local.NoteDao
import com.juandgaines.notemark.note.data.local.NoteEntity
import com.juandgaines.notemark.note.data.local.PendingCreationDao
import com.juandgaines.notemark.note.data.local.PendingCreationEntity
import com.juandgaines.notemark.note.data.mapper.toNote
import com.juandgaines.notemark.note.data.mapper.toNoteEntity
import com.juandgaines.notemark.note.data.remote.RemoteNoteDataSource
import com.juandgaines.notemark.note.domain.Note
import com.juandgaines.notemark.note.domain.NoteRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import timber.log.Timber
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.UUID

class NoteRepositoryImpl(
    private val noteDao: NoteDao,
    private val pendingCreationDao: PendingCreationDao,
    private val remoteNoteDataSource: RemoteNoteDataSource,
    private val applicationScope: CoroutineScope,
) : NoteRepository {

    override fun getNotes(): Flow<List<Note>> {
        return noteDao.getAllNotes()
            .map { entities ->
                entities
                    .filter { it.title.isNotBlank() || it.content.isNotBlank() }
                    .map { it.toNote() }
            }
    }

    override suspend fun fetchNotes(): EmptyResult<DataError> {
        when (val result = remoteNoteDataSource.getNotes()) {
            is Result.Success -> {
                val entities = result.data.map { it.toNoteEntity() }
                noteDao.deleteAllNotes()
                noteDao.upsertNotes(entities)
                pendingCreationDao.deleteAll()
                return Result.Success(Unit)
            }
            is Result.Failure -> return Result.Failure(result.error)
        }
    }

    override suspend fun createBlankNote(): String {
        val id = UUID.randomUUID().toString()
        val now = ZonedDateTime.now(ZoneOffset.UTC)
            .format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'"))
        noteDao.upsertNote(
            NoteEntity(
                id = id,
                title = "",
                content = "",
                createdAt = now,
                lastEditedAt = now,
            )
        )
        pendingCreationDao.insert(PendingCreationEntity(noteId = id))
        return id
    }

    override suspend fun upsertNote(note: Note): EmptyResult<DataError> {
        noteDao.upsertNote(note.toNoteEntity())

        val isPendingCreation = pendingCreationDao.isPendingCreation(note.id)

        applicationScope.launch {
            if (isPendingCreation) {
                when (val result = remoteNoteDataSource.createNote(note)) {
                    is Result.Success -> {
                        pendingCreationDao.deleteByNoteId(note.id)
                        noteDao.deleteNote(note.id)
                        noteDao.upsertNote(result.data.toNoteEntity())
                    }
                    is Result.Failure -> Timber.e("Failed to create note remotely: ${result.error}")
                }
            } else {
                when (val result = remoteNoteDataSource.updateNote(note)) {
                    is Result.Success -> {
                        noteDao.deleteNote(note.id)
                        noteDao.upsertNote(result.data.toNoteEntity())
                    }
                    is Result.Failure -> Timber.e("Failed to update note remotely: ${result.error}")
                }
            }
        }

        return Result.Success(Unit)
    }

    override suspend fun deleteNote(noteId: String): EmptyResult<DataError> {
        val isPendingCreation = pendingCreationDao.isPendingCreation(noteId)
        noteDao.deleteNote(noteId)
        pendingCreationDao.deleteByNoteId(noteId)

        if (!isPendingCreation) {
            applicationScope.launch {
                when (val result = remoteNoteDataSource.deleteNote(noteId)) {
                    is Result.Success -> Unit
                    is Result.Failure -> Timber.e("Failed to delete note remotely: ${result.error}")
                }
            }
        }

        return Result.Success(Unit)
    }

    override suspend fun getNote(noteId: String): Note? {
        return noteDao.getNoteById(noteId)?.toNote()
    }

    override suspend fun deleteNoteIfEmpty(noteId: String) {
        val entity = noteDao.getNoteById(noteId) ?: return
        if (entity.title.isBlank() && entity.content.isBlank()) {
            noteDao.deleteNote(noteId)
            pendingCreationDao.deleteByNoteId(noteId)
        }
    }

    override suspend fun deleteAllNotes() {
        noteDao.deleteAllNotes()
        pendingCreationDao.deleteAll()
    }
}
