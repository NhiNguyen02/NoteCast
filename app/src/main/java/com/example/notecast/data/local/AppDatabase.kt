package com.example.notecast.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.notecast.data.local.dao.*
import com.example.notecast.data.local.entities.*

@Database(
    entities = [AudioEntity::class, TranscriptEntity::class, ProcessedTextEntity::class, NoteEntity::class, FolderEntity::class],
    version = 2,
    exportSchema = false  // đảm bảo true nếu bạn muốn export
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun audioDao(): AudioDao
    abstract fun transcriptDao(): TranscriptDao
    abstract fun processedTextDao(): ProcessedTextDao
    abstract fun noteDao(): NoteDao
    abstract fun folderDao(): FolderDao
}