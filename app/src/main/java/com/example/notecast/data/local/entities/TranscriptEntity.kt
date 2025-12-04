package com.example.notecast.data.local.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "transcript",
    foreignKeys = [ForeignKey(
        entity = NoteEntity::class,
        parentColumns = ["id"],
        childColumns = ["noteId"],
        onDelete = ForeignKey.CASCADE // Xóa Note thì xóa luôn Transcript
    )],
    indices = [Index(value = ["noteId"], unique = true)]
)
data class TranscriptEntity(
    @PrimaryKey val id: String, //
    val noteId: String, // Khóa ngoại liên kết với Note

    val rawText: String, // (Đây là văn bản gốc từ STT)
    val timestampsJson: String?, // Dữ liệu cho bản chép lời (VD: "[{start: 0, end: 1000, text: 'Hello'}]")

    val language: String, //
    val confidence: Float?, //
    val createdAt: Long, //
    @ColumnInfo(defaultValue = "0") val isSynced: Boolean = false, //
    @ColumnInfo(defaultValue = "0") val isDeleted: Boolean = false //
)