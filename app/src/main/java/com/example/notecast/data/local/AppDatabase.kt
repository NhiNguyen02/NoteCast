package com.example.notecast.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.notecast.data.local.dao.AudioDao
import com.example.notecast.data.local.dao.FolderDao
import com.example.notecast.data.local.dao.NoteDao
import com.example.notecast.data.local.dao.ProcessedTextDao
import com.example.notecast.data.local.dao.TranscriptDao
import com.example.notecast.data.local.entities.AudioEntity
import com.example.notecast.data.local.entities.FolderEntity
import com.example.notecast.data.local.entities.NoteEntity
import com.example.notecast.data.local.entities.ProcessedTextEntity
import com.example.notecast.data.local.entities.TranscriptEntity

/**
 * Room database aggregating all DAOs and entities.
 * Increase version and add Migration objects when schema changes.
 */
@Database(
    entities = [AudioEntity::class, TranscriptEntity::class, ProcessedTextEntity::class, NoteEntity::class, FolderEntity::class],
    version = 2,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun audioDao(): AudioDao
    abstract fun transcriptDao(): TranscriptDao
    abstract fun processedTextDao(): ProcessedTextDao
    abstract fun noteDao(): NoteDao
    abstract fun folderDao(): FolderDao
}