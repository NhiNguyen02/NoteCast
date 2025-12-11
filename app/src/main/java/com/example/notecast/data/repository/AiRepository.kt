package com.example.notecast.data.repository

import android.util.Log
import com.example.notecast.BuildConfig
import com.example.notecast.data.remote.GeminiApiService
import com.example.notecast.data.remote.dto.*
import com.example.notecast.domain.model.MindMapNode
import com.example.notecast.domain.model.ProcessedTextData
import kotlinx.serialization.json.Json
import java.util.UUID
import javax.inject.Inject

class AiRepository @Inject constructor(
    private val apiService: GeminiApiService // Inject Service thay vì GenerativeModel
) {
    // Lấy Key từ BuildConfig
    private val apiKey = BuildConfig.GEMINI_API_KEY
    private val jsonParser = Json { ignoreUnknownKeys = true; isLenient = true }

    //Prompt tạo mindmap
    suspend fun generateMindMap(noteContent: String): MindMapNode {
        val prompt = """
            Bạn là chuyên gia tạo Sơ đồ tư duy. Hãy phân tích văn bản sau và tạo cấu trúc JSON Mindmap.
            Yêu cầu:
            1. Xác định Ý chính làm Root.
            2. Phân tách ý phụ thành nhánh con (tối đa 3 cấp).
            3. Nhãn (label) ngắn gọn (< 7 từ).
            4. Màu sắc (colorHex): Root="#6200EE", Con="#F59E2B", "#2ECF9A", "#2F9BFF".
            
            Cấu trúc JSON bắt buộc (Chỉ trả về JSON):
            {
              "root": {
                  "label": "Chủ đề",
                  "colorHex": "#6200EE",
                  "children": [
                     { "label": "Ý 1", "colorHex": "#F59E2B", "children": [] }
                  ]
              }
            }
            
            Văn bản: "$noteContent"
        """.trimIndent()

        return try {
            // 1. Tạo Request
            val request = GeminiRequest(
                contents = listOf(
                    GeminiContent(parts = listOf(GeminiPart(text = prompt)))
                )
            )

            // 2. Gọi API
            val response = apiService.generateContent(apiKey, request)

            // 3. Xử lý kết quả
            val rawText = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                ?: return errorNode("AI trả về rỗng")

            // 4. Làm sạch JSON
            val jsonText = cleanJson(rawText)

            // 5. Parse
            val dto = jsonParser.decodeFromString<MindMapResponseDto>(jsonText)
            mapDtoToDomain(dto.root)

        } catch (e: Exception) {
            e.printStackTrace()
            errorNode("Lỗi: ${e.message}")
        }
    }


    //Prompt xử lý chuẩn hóa hậu kỳ
    suspend fun processNlpPostProcessing(content: String): ProcessedTextData {
        val prompt = """
            Bạn là một hệ thống Xử lý Hậu kỳ NLP (NLP Post-Processing) Tiếng Việt.
            Đầu vào là văn bản thô (raw transcript), có thể thiếu dấu câu và sai chính tả do nhận dạng giọng nói (ví dụ: 'ăn chứa' -> 'ăn chưa').
            
            Nhiệm vụ (Trả về JSON duy nhất):
            1. [ASR Correction & Punctuation]: Sửa lỗi chính tả ASR, thêm dấu câu, viết hoa chuẩn xác.
            
            Văn bản đầu vào: "$content"
            
            Cấu trúc JSON bắt buộc:
            {
              "normalizedText": "Văn bản đã sửa hoàn chỉnh...", 
              "keywords": ["Từ khóa 1", "Từ khóa 2", "..." ] 
            }
        """.trimIndent()

        return try {
            // Tạo Request
            val request = GeminiRequest(
                contents = listOf(GeminiContent(parts = listOf(GeminiPart(text = prompt))))
            )

            // Gọi API
            val response = apiService.generateContent(apiKey, request)

            // Lấy kết quả thô
            val rawText = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                ?: throw Exception("AI không trả về dữ liệu")

            // Làm sạch và Parse JSON
            val jsonText = cleanJson(rawText)
            jsonParser.decodeFromString<ProcessedTextData>(jsonText)

        } catch (e: Exception) {
            e.printStackTrace()
            // Fallback: Nếu lỗi, trả về văn bản gốc và không có keywords
            ProcessedTextData(
                normalizedText = content,
                keywords = emptyList()
            )
        }
    }

    private fun cleanJson(text: String): String {
        var result = text
        if (result.contains("```")) {
            result = result.replace("```json", "").replace("```", "").trim()
        }
        val start = result.indexOf("{")
        val end = result.lastIndexOf("}")
        if (start != -1 && end != -1) {
            result = result.substring(start, end + 1)
        }
        return result
    }

    private fun mapDtoToDomain(dto: MindMapNodeDto): MindMapNode {
        return MindMapNode(
            id = UUID.randomUUID().toString(),
            label = dto.label,
            colorHex = dto.colorHex ?: "#6200EE",
            children = dto.children.map { mapDtoToDomain(it) }
        )
    }

    private fun errorNode(msg: String) = MindMapNode(label = msg, colorHex = "#FF0000")
}