package com.example.notecast.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class Transcript(
    val id: String,
    val audioId: String,
    val rawText: String,
    val language: String,
    val confidence: Float? = null,
    val createdAt: Long,
    val isSynced: Boolean = false,
    val isDeleted: Boolean = false
)