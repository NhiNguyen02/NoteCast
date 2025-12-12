package com.example.notecast.domain.model

import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
data class SummarizeNode(
    val id: String = UUID.randomUUID().toString(),
    val title: String,                 // Tiêu đề đoạn tóm tắt
    val content: String,               // Nội dung tóm tắt chính
    val bullets: List<String> = emptyList(), // Các ý chính (tuỳ chọn)
    val children: List<SummarizeNode> = emptyList() // Nếu tóm tắt theo dạng cây
)
