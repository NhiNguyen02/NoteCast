package com.example.notecast.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.notecast.data.local.dao.FolderDao
import com.example.notecast.data.local.dao.NoteDao
import com.example.notecast.data.local.entities.*

@Database(
    // 1. Khai báo tất cả 5 BẢNG (Entities)
    entities = [
        FolderEntity::class,
        NoteEntity::class,
        AudioEntity::class,
        TranscriptEntity::class,
        ProcessedTextEntity::class
    ],
    version = 1, // Bắt đầu lại từ version 1 với schema mới này
    exportSchema = false
)
@TypeConverters(Converters::class) // File này chúng ta sẽ tạo ở bước tiếp theo
abstract class AppDatabase : RoomDatabase() {

    // 2. Chỉ cần cung cấp 2 DAOs
    abstract fun noteDao(): NoteDao
    abstract fun folderDao(): FolderDao


}