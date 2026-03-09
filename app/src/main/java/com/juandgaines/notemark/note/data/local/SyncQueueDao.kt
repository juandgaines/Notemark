package com.juandgaines.notemark.note.data.local

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface SyncQueueDao {
    @Upsert
    suspend fun upsert(entity: SyncQueueEntity)

    @Query("DELETE FROM sync_queue WHERE id = :id")
    suspend fun delete(id: String)

    @Query("DELETE FROM sync_queue WHERE noteId = :noteId")
    suspend fun deleteByNoteId(noteId: String)

    @Query("SELECT * FROM sync_queue WHERE userId = :userId ORDER BY timestamp ASC")
    suspend fun getAllByUserId(userId: String): List<SyncQueueEntity>

    @Query("SELECT COUNT(*) FROM sync_queue WHERE userId = :userId")
    fun getCountByUserId(userId: String): Flow<Int>

    @Query("DELETE FROM sync_queue")
    suspend fun deleteAll()

    @Query("DELETE FROM sync_queue WHERE userId = :userId")
    suspend fun deleteAllByUserId(userId: String)
}
