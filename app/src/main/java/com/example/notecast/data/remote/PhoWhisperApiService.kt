package com.example.notecast.data.remote

import com.example.notecast.data.remote.dto.PhoWhisperTranscribeResponse
import com.example.notecast.data.remote.dto.PhoWhisperTranscribeUrlRequest
import retrofit2.http.Body
import retrofit2.http.POST

/**
 * PhoWhisperApiService
 *
 * Retrofit service gọi backend PhoWhisper (HF Space) qua endpoint `/transcribe-url`.
 * Payload: { "audio_url": "<downloadUrl>" }
 * Response: chứa transcript đầy đủ + thông tin audio + danh sách chunks.
 */
interface PhoWhisperApiService {

    @POST("transcribe-url")
    suspend fun transcribeUrl(
        @Body request: PhoWhisperTranscribeUrlRequest
    ): PhoWhisperTranscribeResponse
}

