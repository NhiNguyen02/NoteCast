package com.example.notecast.data.local.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "processed_text",
    foreignKeys = [ForeignKey(
        entity = NoteEntity::class,
        parentColumns = ["id"],
        childColumns = ["noteId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index(value = ["noteId"], unique = true)]
)
data class ProcessedTextEntity(
    @PrimaryKey val id: String, //
    val noteId: String, // Khóa ngoại liên kết với Note


    val punctuatedText: String?, // (Văn bản đã chuẩn hóa)
    val summary: String?, // (Văn bản tóm tắt)
    val sentiment: String?, //
    val createdAt: Long, //
    @ColumnInfo(defaultValue = "0") val isSynced: Boolean = false, //
    @ColumnInfo(defaultValue = "0") val isDeleted: Boolean = false //
)