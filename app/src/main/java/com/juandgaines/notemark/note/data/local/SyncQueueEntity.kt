package com.juandgaines.notemark.note.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class SyncOperation { CREATE, UPDATE, DELETE }

@Entity(tableName = "sync_queue")
data class SyncQueueEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val noteId: String,
    val operation: SyncOperation,
    val payload: String,
    val timestamp: Long,
)
