package com.example.notecast.data.local.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.ForeignKey.Companion.CASCADE

@Entity(
    tableName = "transcript",
    foreignKeys = [ForeignKey(
        entity = AudioEntity::class,
        parentColumns = ["id"],
        childColumns = ["audioId"],
        onDelete = CASCADE
    )],
    indices = [Index(value = ["audioId"])]
)
data class TranscriptEntity(
    @PrimaryKey val id: String,
    val audioId: String,
    val rawText: String,
    val language: String,
    val confidence: Float?,
    val createdAt: Long,
    @ColumnInfo(defaultValue = "0") val isSynced: Boolean = false,
    @ColumnInfo(defaultValue = "0") val isDeleted: Boolean = false
)