package com.example.notecast.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.notecast.data.local.dao.FolderDao
import com.example.notecast.data.local.dao.NoteDao
import com.example.notecast.data.local.entities.AudioEntity
import com.example.notecast.data.local.entities.FolderEntity
import com.example.notecast.data.local.entities.NoteEntity

@Database(
    entities = [
        FolderEntity::class,
        NoteEntity::class,
        AudioEntity::class,
    ],
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun noteDao(): NoteDao
    abstract fun folderDao(): FolderDao

    companion object {
        const val DB_NAME = "notecast.db"
    }
}