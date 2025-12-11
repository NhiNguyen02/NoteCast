package com.example.notecast.domain.model
import kotlinx.serialization.Serializable

@Serializable
data class ProcessedTextData(
    val normalizedText: String, // Văn bản đã sửa lỗi
    val keywords: List<String> = emptyList() // Từ khóa
)