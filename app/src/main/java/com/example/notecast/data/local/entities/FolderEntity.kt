package com.example.notecast.data.local.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Optional: folder to organize notes, with a color.
 */
@Entity(tableName = "folder")
data class FolderEntity(
    @PrimaryKey val id: String,
    val name: String,
    val colorHex: String? = null,
    val createdAt: Long,
    val updatedAt: Long,
    @ColumnInfo(defaultValue = "0") val isSynced: Boolean = false,
    @ColumnInfo(defaultValue = "0") val isDeleted: Boolean = false
)