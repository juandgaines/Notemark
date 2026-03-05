package com.juandgaines.notemark.core.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.juandgaines.notemark.note.data.local.NoteDao
import com.juandgaines.notemark.note.data.local.NoteEntity
import com.juandgaines.notemark.note.data.local.PendingCreationDao
import com.juandgaines.notemark.note.data.local.PendingCreationEntity

@Database(
    entities = [NoteEntity::class, PendingCreationEntity::class],
    version = 1,
    exportSchema = true,
)
abstract class NotemarkDatabase : RoomDatabase() {
    abstract val noteDao: NoteDao
    abstract val pendingCreationDao: PendingCreationDao
}
