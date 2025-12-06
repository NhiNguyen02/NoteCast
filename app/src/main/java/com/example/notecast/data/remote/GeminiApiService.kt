package com.example.notecast.data.remote

import com.example.notecast.data.remote.dto.GeminiRequest
import com.example.notecast.data.remote.dto.GeminiResponse
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query

interface GeminiApiService {
    // Gọi model gemini-1.5-flash-latest (bản mới nhất ổn định)
    @POST("v1beta/models/gemini-2.5-flash:generateContent")
    suspend fun generateContent(
        @Query("key") apiKey: String,
        @Body request: GeminiRequest
    ): GeminiResponse
}