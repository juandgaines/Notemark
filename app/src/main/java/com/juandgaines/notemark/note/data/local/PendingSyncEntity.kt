package com.juandgaines.notemark.note.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "pending_creation")
data class PendingCreationEntity(
    @PrimaryKey val noteId: String,
)
