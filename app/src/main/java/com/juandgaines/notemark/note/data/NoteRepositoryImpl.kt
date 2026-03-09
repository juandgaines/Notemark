package com.juandgaines.notemark.note.data

import com.juandgaines.notemark.core.domain.SessionStorage
import com.juandgaines.notemark.core.domain.util.DataError
import com.juandgaines.notemark.core.domain.util.EmptyResult
import com.juandgaines.notemark.core.domain.util.Result
import com.juandgaines.notemark.core.domain.util.asEmptyResult
import com.juandgaines.notemark.core.domain.util.onFailure
import com.juandgaines.notemark.core.domain.util.onSuccess
import com.juandgaines.notemark.note.data.local.NoteDao
import com.juandgaines.notemark.note.data.local.NoteEntity
import com.juandgaines.notemark.note.data.local.PendingCreationDao
import com.juandgaines.notemark.note.data.local.PendingCreationEntity
import com.juandgaines.notemark.note.data.local.SyncOperation
import com.juandgaines.notemark.note.data.local.SyncQueueDao
import com.juandgaines.notemark.note.data.local.SyncQueueEntity
import com.juandgaines.notemark.note.data.mapper.toNote
import com.juandgaines.notemark.note.data.mapper.toNoteEntity
import com.juandgaines.notemark.note.data.remote.RemoteNoteDataSource
import com.juandgaines.notemark.note.domain.Note
import com.juandgaines.notemark.note.domain.NoteRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString
import timber.log.Timber
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.UUID

class NoteRepositoryImpl(
    private val noteDao: NoteDao,
    private val pendingCreationDao: PendingCreationDao,
    private val syncQueueDao: SyncQueueDao,
    private val remoteNoteDataSource: RemoteNoteDataSource,
    private val sessionStorage: SessionStorage,
    private val applicationScope: CoroutineScope,
    private val json: Json,
) : NoteRepository {

    override fun getNotes(): Flow<List<Note>> {
        return noteDao.getAllNotesByLastEditedAt()
            .map { entities ->
                entities
                    .filter { it.title.isNotBlank() || it.content.isNotBlank() }
                    .map { it.toNote() }
            }
    }

    override suspend fun fetchNotes(): EmptyResult<DataError> {
        return remoteNoteDataSource.getNotes()
            .onSuccess { remoteNotes ->
                applicationScope.launch {
                    mergeRemoteNotes(remoteNotes)
                }.join()
            }
            .asEmptyResult()
    }

    private suspend fun mergeRemoteNotes(remoteNotes: List<Note>) {
        val localEntities = noteDao.getAllNotes()
        val localMap = localEntities.associateBy { it.id }
        val pendingNoteIds = syncQueueDao.getAllByUserId(
            sessionStorage.get()?.username ?: ""
        ).map { it.noteId }.toSet()

        supervisorScope {
            // Upsert new or updated remote notes using last-write-wins
            val upsertJob = launch {
                upsertRemoteNotes(remoteNotes, localMap, pendingNoteIds)
            }

            // Remove local notes deleted on the server
            val cleanupJob = launch {
                removeDeletedNotes(localEntities, remoteNotes, pendingNoteIds)
            }

            upsertJob.join()
            cleanupJob.join()
        }
    }

    /**
     * For each remote note: insert if new, overwrite if remote is newer (last-write-wins),
     * or skip if the note has pending local changes in the sync queue.
     */
    private suspend fun upsertRemoteNotes(
        remoteNotes: List<Note>,
        localMap: Map<String, NoteEntity>,
        pendingNoteIds: Set<String>,
    ) {
        for (remoteNote in remoteNotes) {
            val remoteEntity = remoteNote.toNoteEntity()
            val localEntity = localMap[remoteNote.id]

            if (localEntity == null) {
                noteDao.upsertNote(remoteEntity)
            } else if (remoteNote.id !in pendingNoteIds) {
                if (remoteEntity.lastEditedAt > localEntity.lastEditedAt) {
                    noteDao.upsertNote(remoteEntity)
                }
            }
        }
    }

    /**
     * Delete local notes that no longer exist on the server, unless they have
     * pending sync operations or are pending creation (created offline).
     */
    private suspend fun removeDeletedNotes(
        localEntities: List<NoteEntity>,
        remoteNotes: List<Note>,
        pendingNoteIds: Set<String>,
    ) {
        val remoteIds = remoteNotes.map { it.id }.toSet()
        for (localEntity in localEntities) {
            if (localEntity.id !in remoteIds && localEntity.id !in pendingNoteIds) {
                val isPendingCreation = pendingCreationDao.isPendingCreation(localEntity.id)
                if (!isPendingCreation) {
                    noteDao.deleteNote(localEntity.id)
                }
            }
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
        val operation = if (isPendingCreation) SyncOperation.CREATE else SyncOperation.UPDATE

        applicationScope.launch {
            if (isPendingCreation) {
                remoteNoteDataSource.createNote(note)
                    .onSuccess { remoteNote ->
                        pendingCreationDao.deleteByNoteId(note.id)
                        syncQueueDao.deleteByNoteId(note.id)
                        noteDao.deleteNote(note.id)
                        noteDao.upsertNote(remoteNote.toNoteEntity())
                    }
                    .onFailure { error ->
                        Timber.e("Failed to create note remotely: $error")
                        enqueueSync(note, operation)
                    }
            } else {
                remoteNoteDataSource.updateNote(note)
                    .onSuccess { remoteNote ->
                        syncQueueDao.deleteByNoteId(note.id)
                        noteDao.deleteNote(note.id)
                        noteDao.upsertNote(remoteNote.toNoteEntity())
                    }
                    .onFailure { error ->
                        Timber.e("Failed to update note remotely: $error")
                        enqueueSync(note, operation)
                    }
            }
        }

        return Result.Success(Unit)
    }

    override suspend fun deleteNote(noteId: String): EmptyResult<DataError> {
        val isPendingCreation = pendingCreationDao.isPendingCreation(noteId)
        noteDao.deleteNote(noteId)
        pendingCreationDao.deleteByNoteId(noteId)
        syncQueueDao.deleteByNoteId(noteId)

        if (!isPendingCreation) {
            applicationScope.launch {
                remoteNoteDataSource.deleteNote(noteId)
                    .onFailure { error ->
                        Timber.e("Failed to delete note remotely: $error")
                        val userId = sessionStorage.get()?.username ?: return@onFailure
                        syncQueueDao.upsert(
                            SyncQueueEntity(
                                id = UUID.randomUUID().toString(),
                                userId = userId,
                                noteId = noteId,
                                operation = SyncOperation.DELETE,
                                payload = "",
                                timestamp = System.currentTimeMillis(),
                            )
                        )
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
            syncQueueDao.deleteByNoteId(noteId)
        }
    }

    override suspend fun clearAllNotes() {
        noteDao.deleteAllNotes()
        pendingCreationDao.deleteAll()
    }

    override suspend fun syncPendingItems() {
        val userId = sessionStorage.get()?.username ?: return
        val pendingItems = syncQueueDao.getAllByUserId(userId)
        if (pendingItems.isEmpty()) return

        // Group by noteId and resolve to a single effective operation per note
        val resolvedOps = resolveOperations(pendingItems)

        // Process all notes in parallel
        supervisorScope {
            resolvedOps.map { (noteId, resolved) ->
                async { syncResolvedOperation(noteId, resolved) }
            }.awaitAll()
        }
    }

    private data class ResolvedOperation(
        val operation: SyncOperation,
        val payload: String,
        val queueIds: List<String>,
    )

    private fun resolveOperations(
        items: List<SyncQueueEntity>,
    ): Map<String, ResolvedOperation> {
        return items
            .groupBy { it.noteId }
            .mapValues { (_, ops) ->
                val sorted = ops.sortedBy { it.timestamp }
                val hasDelete = sorted.any { it.operation == SyncOperation.DELETE }
                val allIds = sorted.map { it.id }

                if (hasDelete) {
                    // DELETE wins over everything
                    ResolvedOperation(
                        operation = SyncOperation.DELETE,
                        payload = "",
                        queueIds = allIds,
                    )
                } else {
                    val hasCreate = sorted.any { it.operation == SyncOperation.CREATE }
                    val latestPayload = sorted.last().payload
                    // CREATE + UPDATE(s) → single CREATE with latest payload
                    // UPDATE + UPDATE(s) → single UPDATE with latest payload
                    ResolvedOperation(
                        operation = if (hasCreate) SyncOperation.CREATE else SyncOperation.UPDATE,
                        payload = latestPayload,
                        queueIds = allIds,
                    )
                }
            }
    }

    private suspend fun syncResolvedOperation(noteId: String, resolved: ResolvedOperation) {
        when (resolved.operation) {
            SyncOperation.CREATE -> {
                val note = deserializeNote(resolved.payload) ?: run {
                    clearQueueEntries(resolved.queueIds)
                    return
                }
                remoteNoteDataSource.createNote(note)
                    .onSuccess { remoteNote ->
                        clearQueueEntries(resolved.queueIds)
                        pendingCreationDao.deleteByNoteId(noteId)
                        noteDao.deleteNote(noteId)
                        noteDao.upsertNote(remoteNote.toNoteEntity())
                    }
                    .onFailure { error ->
                        Timber.e("syncPendingItems: CREATE failed for $noteId: $error")
                    }
            }
            SyncOperation.UPDATE -> {
                val note = deserializeNote(resolved.payload) ?: run {
                    clearQueueEntries(resolved.queueIds)
                    return
                }
                remoteNoteDataSource.updateNote(note)
                    .onSuccess { remoteNote ->
                        clearQueueEntries(resolved.queueIds)
                        noteDao.deleteNote(noteId)
                        noteDao.upsertNote(remoteNote.toNoteEntity())
                    }
                    .onFailure { error ->
                        Timber.e("syncPendingItems: UPDATE failed for $noteId: $error")
                    }
            }
            SyncOperation.DELETE -> {
                remoteNoteDataSource.deleteNote(noteId)
                    .onSuccess {
                        clearQueueEntries(resolved.queueIds)
                    }
                    .onFailure { error ->
                        Timber.e("syncPendingItems: DELETE failed for $noteId: $error")
                    }
            }
        }
    }

    private suspend fun clearQueueEntries(ids: List<String>) {
        ids.forEach { syncQueueDao.delete(it) }
    }

    override fun hasPendingSyncs(userId: String): Flow<Boolean> {
        return syncQueueDao.getCountByUserId(userId).map { it > 0 }
    }

    private suspend fun enqueueSync(note: Note, operation: SyncOperation) {
        val userId = sessionStorage.get()?.username ?: return
        val payload = serializeNote(note)
        syncQueueDao.upsert(
            SyncQueueEntity(
                id = UUID.randomUUID().toString(),
                userId = userId,
                noteId = note.id,
                operation = operation,
                payload = payload,
                timestamp = System.currentTimeMillis(),
            )
        )
    }

    private fun serializeNote(note: Note): String {
        val entity = note.toNoteEntity()
        return json.encodeToString(
            NotePayload(
                id = entity.id,
                title = entity.title,
                content = entity.content,
                createdAt = entity.createdAt,
                lastEditedAt = entity.lastEditedAt,
            )
        )
    }

    private fun deserializeNote(payload: String): Note? {
        return try {
            val notePayload = json.decodeFromString<NotePayload>(payload)
            NoteEntity(
                id = notePayload.id,
                title = notePayload.title,
                content = notePayload.content,
                createdAt = notePayload.createdAt,
                lastEditedAt = notePayload.lastEditedAt,
            ).toNote()
        } catch (e: Exception) {
            Timber.e(e, "Failed to deserialize note payload")
            null
        }
    }
}

@kotlinx.serialization.Serializable
private data class NotePayload(
    val id: String,
    val title: String,
    val content: String,
    val createdAt: String,
    val lastEditedAt: String,
)
