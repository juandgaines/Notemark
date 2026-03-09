package com.juandgaines.notemark.core.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.juandgaines.notemark.note.data.local.NoteDao
import com.juandgaines.notemark.note.data.local.NoteEntity
import com.juandgaines.notemark.note.data.local.PendingCreationDao
import com.juandgaines.notemark.note.data.local.PendingCreationEntity
import com.juandgaines.notemark.note.data.local.SyncQueueDao
import com.juandgaines.notemark.note.data.local.SyncQueueEntity

@Database(
    entities = [NoteEntity::class, PendingCreationEntity::class, SyncQueueEntity::class],
    version = 1,
    exportSchema = true,
)
abstract class NotemarkDatabase : RoomDatabase() {
    abstract val noteDao: NoteDao
    abstract val pendingCreationDao: PendingCreationDao
    abstract val syncQueueDao: SyncQueueDao
}
