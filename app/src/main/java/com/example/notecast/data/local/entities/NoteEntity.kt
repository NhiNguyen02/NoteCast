package com.example.notecast.data.local.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import androidx.room.ForeignKey.Companion.SET_NULL

/**
 * Note entity that may reference processed_text.
 * - tags: stored as JSON string (use TypeConverter)
 * - processedTextId: nullable, ON DELETE SET NULL (keep note if processed_text removed)
 */
@Entity(
    tableName = "note",
    foreignKeys = [ForeignKey(
        entity = ProcessedTextEntity::class,
        parentColumns = ["id"],
        childColumns = ["processedTextId"],
        onDelete = SET_NULL
    )]
)
data class NoteEntity(
    @PrimaryKey val id: String,
    val title: String,
    val processedTextId: String?,
    val tags: String, // JSON string (List<String>) via Converters
    val isFavorite: Boolean = false,
    val folderId: String? = null,
    val colorHex: String? = null,
    val updatedAt: Long,
    @ColumnInfo(defaultValue = "0") val isSynced: Boolean = false,
    @ColumnInfo(defaultValue = "0") val isDeleted: Boolean = false
)