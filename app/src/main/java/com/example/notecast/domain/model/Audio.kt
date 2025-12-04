package com.example.notecast.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class Audio(
    val id: String,
    val filePath: String,       // local path
    val cloudUrl: String? = null,
    val durationMs: Long,
    val sampleRate: Int,
    val channels: Int,
    val createdAt: Long,
    val isSynced: Boolean = false,
    val isDeleted: Boolean = false
)