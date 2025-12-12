package com.example.notecast.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class Transcript(
    val id: String,
    val audioId: String,
    val rawText: String,
)