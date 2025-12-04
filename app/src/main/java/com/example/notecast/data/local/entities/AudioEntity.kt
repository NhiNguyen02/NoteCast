package com.example.notecast.data.local.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "audio",
    foreignKeys = [ForeignKey(
        entity = NoteEntity::class,
        parentColumns = ["id"],
        childColumns = ["noteId"],
        onDelete = ForeignKey.CASCADE // Xóa Note thì xóa luôn Audio
    )],
    // Đặt noteId làm PK để đảm bảo quan hệ 1-1
    indices = [Index(value = ["noteId"], unique = true)]
)
data class AudioEntity(
    @PrimaryKey val id: String, // (Giữ PK riêng nếu bạn muốn)
    val noteId: String, // Khóa ngoại liên kết với Note

    val filePath: String, //
    val cloudUrl: String? = null, //
    val durationMs: Long, //
    val sampleRate: Int, //
    val channels: Int, //
    val createdAt: Long, //
    @ColumnInfo(defaultValue = "0") val isSynced: Boolean = false, //
    @ColumnInfo(defaultValue = "0") val isDeleted: Boolean = false //
)