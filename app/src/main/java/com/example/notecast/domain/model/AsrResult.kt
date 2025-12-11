package com.example.notecast.domain.model

/**
 * Kết quả ASR đầy đủ cho một phiên ghi âm.
 */
data class AsrResult(
    val text: String,
    val durationSec: Double,
    val sampleRate: Int,
    val chunks: List<ChunkResult>
)

