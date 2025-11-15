package com.example.notecast.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class ProcessedText(
    val id: String,
    val transcriptId: String,
    val punctuatedText: String? = null,
    val summary: String? = null,
    val sentiment: String? = null,
    val createdAt: Long,
    val isSynced: Boolean = false,
    val isDeleted: Boolean = false
)