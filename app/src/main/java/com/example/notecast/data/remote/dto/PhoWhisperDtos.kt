package com.example.notecast.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * DTO cho API PhoWhisper `/transcribe-url`.
 */

@Serializable
data class PhoWhisperTranscribeUrlRequest(
    @SerialName("audio_url") val audioUrl: String
)

@Serializable
data class PhoWhisperChunkDto(
    val start: Double,
    val end: Double,
    val text: String
)

@Serializable
data class PhoWhisperTranscribeResponse(
    val text: String,
    val duration: Double,
    @SerialName("sample_rate") val sampleRate: Int,
    val chunks: List<PhoWhisperChunkDto> = emptyList()
)
