package com.example.notecast.data.remote

import kotlinx.serialization.Serializable
import retrofit2.http.Body
import retrofit2.http.POST

/**
 * Retrofit client cho PhoWhisper ASR service (transcribe-url).
 */
interface PhoWhisperApi {

    @POST("transcribe-url")
    suspend fun transcribeUrl(
        @Body body: TranscribeUrlRequest,
    ): TranscribeUrlResponse
}

@Serializable
data class TranscribeUrlRequest(
    val audio_url: String,
    val user_id: String? = null,
)

@Serializable
data class TranscribeUrlResponse(
    val note_id: String,
    val status: String,
    val duration: Double? = null,
    val job_id: String? = null,
)

