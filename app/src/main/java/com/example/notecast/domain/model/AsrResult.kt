package com.example.notecast.domain.model

import kotlinx.serialization.Serializable

/**
 * Kết quả ASR đầy đủ cho một phiên ghi âm.
 */
@Serializable
data class AsrResult(
    val text: String,
    val durationSec: Double,
    val sampleRate: Int,
    val chunks: List<ChunkResult>
)

