package com.example.notecast.domain.model

import kotlinx.serialization.Serializable

@Serializable
enum class NoteType { AUDIO, TEXT }

@Serializable
enum class NoteStatus { CREATED, PROCESSING, READY, ERROR }

@Serializable
data class AudioChunk(
    val start: Double,
    val end: Double,
    val text: String,
)

@Serializable
data class AudioDomain(
    val durationSec: Double,
    val sampleRate: Int,
    val chunks: List<AudioChunk>,
    val localFilePath: String? = null,
    val cloudUrl: String? = null,
)

@Serializable
data class NoteDomain(
    val id: String,
    val type: NoteType,
    val title: String?,
    val rawText: String?,
    val normalizedText: String?,
    val keywords: List<String>,
    val summary: String?,
    val mindmapJson: String?,
    val audio: AudioDomain?,
    val folderId: String?,
    val status: NoteStatus,
    val statusRaw: String? = null,
    val isFavorite: Boolean = false,
    val isPinned: Boolean = false,
    val createdAt: Long,
    val updatedAt: Long,
)
