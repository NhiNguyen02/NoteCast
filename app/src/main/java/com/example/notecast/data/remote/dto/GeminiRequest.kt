package com.example.notecast.data.remote.dto

import kotlinx.serialization.Serializable

// --- REQUEST (Gửi đi) ---
@Serializable
data class GeminiRequest(
    val contents: List<GeminiContent>,
    val generationConfig: GeminiConfig? = null   // <— Cho phép null để tuỳ use-case
)

@Serializable
data class GeminiContent(
    val role: String = "user",
    val parts: List<GeminiPart>
)

@Serializable
data class GeminiPart(val text: String)

@Serializable
data class GeminiConfig(
    val responseMimeType: String? = null         // <— Cho phép override
)

// --- RESPONSE (Nhận về) ---
@Serializable
data class GeminiResponse(
    val candidates: List<GeminiCandidate>? = null,
    val error: GeminiError? = null
)

@Serializable
data class GeminiCandidate(
    val content: GeminiContent? = null
)

@Serializable
data class GeminiError(
    val code: Int,
    val message: String,
    val status: String
)

// --- MINDMAP JSON ---
@Serializable
data class MindMapResponseDto(
    val root: MindMapNodeDto
)

@Serializable
data class MindMapNodeDto(
    val label: String = "Ý tưởng",
    val colorHex: String? = null,
    val children: List<MindMapNodeDto> = emptyList()
)
