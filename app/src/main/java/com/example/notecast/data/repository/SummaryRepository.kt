package com.example.notecast.data.repository

import com.example.notecast.BuildConfig
import com.example.notecast.data.remote.GeminiApiService
import com.example.notecast.data.remote.dto.GeminiContent
import com.example.notecast.data.remote.dto.GeminiPart
import com.example.notecast.data.remote.dto.GeminiRequest
import kotlinx.serialization.json.Json
import javax.inject.Inject

class SummaryRepository @Inject constructor(
    private val apiService: GeminiApiService
) {
    private val apiKey = BuildConfig.GEMINI_API_KEY
    private val jsonParser = Json { ignoreUnknownKeys = true; isLenient = true }

    suspend fun summarize(noteContent: String): String {

        val prompt = """
            Bạn là chuyên gia tóm tắt. Hãy tóm tắt văn bản sau thành **một đoạn văn duy nhất**.
            
            Yêu cầu:
            1. Viết khoảng 3-5 câu, tổng hợp đầy đủ chủ đề và các ý chính.
            2. Viết liền mạch, KHÔNG xuống dòng, KHÔNG dùng gạch đầu dòng hay đánh số.
            3. Chỉ dựa trên thông tin được cung cấp, tuyệt đối KHÔNG tự thêm thông tin bên ngoài.
            4. Trả về văn bản thuần (plain text).
        
            Văn bản: "$noteContent"
        """.trimIndent()

        return try {
            // ... giữ nguyên phần gọi API ...
            val request = GeminiRequest(
                contents = listOf(
                    GeminiContent(parts = listOf(GeminiPart(text = prompt)))
                )
            )

            val response = apiService.generateContent(apiKey, request)

            val rawText = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                ?: return "Lỗi: AI trả về rỗng"

            cleanText(rawText)
        } catch (e: Exception) {
            e.printStackTrace()
            "Lỗi: ${e.message ?: "Không xác định"}"
        }
    }

    private fun cleanText(text: String): String {
        var result = text.trim()
        // Remove code fences if present
        if (result.contains("```")) {
            result = result.replace("```", "").trim()
        }
        // If wrapped in quotes, remove them
        if ((result.startsWith("\"") && result.endsWith("\"")) || (result.startsWith("'") && result.endsWith("'"))) {
            result = result.substring(1, result.length - 1).trim()
        }
        return result
    }
}