package com.juandgaines.notemark.note.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface PendingCreationDao {
    @Query("SELECT EXISTS(SELECT 1 FROM pending_creation WHERE noteId = :noteId)")
    suspend fun isPendingCreation(noteId: String): Boolean

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: PendingCreationEntity)

    @Query("DELETE FROM pending_creation WHERE noteId = :noteId")
    suspend fun deleteByNoteId(noteId: String)

    @Query("DELETE FROM pending_creation")
    suspend fun deleteAll()
}
