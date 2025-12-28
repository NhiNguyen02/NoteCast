package com.example.notecast.data.local.entities

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
    @PrimaryKey val noteId: String, // Khóa ngoại liên kết với Note

    val durationSec: Double, //
    val sampleRate: Int, //
    val chunksJson: String? = null, //
    val localFilePath: String? = null, //
    val cloudUrl: String? = null, //
    val createdAt: Long, //
    val asrModel: String? = null,
)