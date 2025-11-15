package com.example.notecast.data.local.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Audio metadata stored locally.
 * - filePath: local file path
 * - cloudUrl: remote uploaded URL (Cloudinary / Firebase Storage)
 * - isSynced/isDeleted: local sync metadata
 */
@Entity(tableName = "audio")
data class AudioEntity(
    @PrimaryKey val id: String,
    val filePath: String,
    val cloudUrl: String? = null,
    val durationMs: Long,
    val sampleRate: Int,
    val channels: Int,
    val createdAt: Long,
    @ColumnInfo(defaultValue = "0") val isSynced: Boolean = false,
    @ColumnInfo(defaultValue = "0") val isDeleted: Boolean = false
)