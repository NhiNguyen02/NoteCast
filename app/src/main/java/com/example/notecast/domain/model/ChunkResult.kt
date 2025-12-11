package com.example.notecast.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class ChunkResult(
    val startSec: Double,
    val endSec: Double,
    val text: String
)
