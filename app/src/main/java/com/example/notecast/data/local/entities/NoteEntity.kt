package com.example.notecast.data.local.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "note",
    foreignKeys = [ForeignKey(
        entity = FolderEntity::class,
        parentColumns = ["id"],
        childColumns = ["folderId"],
        onDelete = ForeignKey.SET_NULL // Nếu xóa folder, note sẽ về "chưa phân loại"
    )],
    indices = [Index(value = ["folderId"])]
)
data class NoteEntity(
    @PrimaryKey val id: String,

    val noteType: String, // "TEXT" hoặc "VOICE"
    val title: String,
    val content: String? = null,
    val tags: String,
    val mindMapJson: String? = null,

    val isFavorite: Boolean = false,

    @ColumnInfo(defaultValue = "NULL")
    val pinTimestamp: Long? = null,

    val folderId: String? = null,
    val colorHex: String? = null,
    val createdAt: Long,
    val updatedAt: Long,
    @ColumnInfo(defaultValue = "0") val isSynced: Boolean = false,
    @ColumnInfo(defaultValue = "0") val isDeleted: Boolean = false
)