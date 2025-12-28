package com.example.notecast.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * DTO cho API PhoWhisper `/transcribe-url`.
 */

@Serializable
data class PhoWhisperTranscribeUrlRequest(
    @SerialName("audio_url") val audioUrl: String,
    @SerialName("user_id") val userId: String
)

@Serializable
data class PhoWhisperTranscribeResponse(
    @SerialName("note_id") val noteId: String,
    @SerialName("status") val status: String,
    @SerialName("duration") val duration: Double? = null,
)
