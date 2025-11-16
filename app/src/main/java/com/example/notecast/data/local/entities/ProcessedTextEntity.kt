package com.example.notecast.data.local.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.ForeignKey.Companion.CASCADE

@Entity(
    tableName = "processed_text",
    foreignKeys = [ForeignKey(
        entity = TranscriptEntity::class,
        parentColumns = ["id"],
        childColumns = ["transcriptId"],
        onDelete = CASCADE
    )],
    indices = [Index(value = ["transcriptId"])]
)
data class ProcessedTextEntity(
    @PrimaryKey val id: String,
    val transcriptId: String,
    val punctuatedText: String?,
    val summary: String?,
    val sentiment: String?,
    val createdAt: Long,
    @ColumnInfo(defaultValue = "0") val isSynced: Boolean = false,
    @ColumnInfo(defaultValue = "0") val isDeleted: Boolean = false
)