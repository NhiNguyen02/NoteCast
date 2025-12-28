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

    val type: String,
    val title: String? = null,
    val rawText: String? = null,
    val normalizedText: String? = null,
    val summary: String? = null,
    val keywordsJson: String? = null, // JSON List<String>
    val mindmapJson: String? = null,  // JSON MindMapNode tree
    val status: String,

    // MỚI: lưu danh sách tác vụ generate backend đã chạy
    val generateJson: String? = null,     // JSON của List<String>

    val folderId: String? = null,
    val createdAt: Long,
    val updatedAt: Long,
    @ColumnInfo(defaultValue = "0") val isSynced: Boolean = false,
    @ColumnInfo(defaultValue = "0") val isDeleted: Boolean = false,
    @ColumnInfo(defaultValue = "0") val isFavorite: Boolean = false,
    @ColumnInfo(defaultValue = "0") val isPinned: Boolean = false,
)