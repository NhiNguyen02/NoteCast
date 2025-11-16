package com.example.notecast.data.local.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.ForeignKey.Companion.SET_NULL

@Entity(
    tableName = "note",
    foreignKeys = [ForeignKey(
        entity = ProcessedTextEntity::class,
        parentColumns = ["id"],
        childColumns = ["processedTextId"],
        onDelete = SET_NULL
    )],
    indices = [Index(value = ["processedTextId"])]
)
data class NoteEntity(
    @PrimaryKey val id: String,
    val title: String,
    val content: String? = null,
    val processedTextId: String?,
    val tags: String,
    val mindMapJson: String? = null,
    val isFavorite: Boolean = false,
    val folderId: String? = null,
    val colorHex: String? = null,
    val updatedAt: Long,
    @ColumnInfo(defaultValue = "0") val isSynced: Boolean = false,
    @ColumnInfo(defaultValue = "0") val isDeleted: Boolean = false
)