package com.example.notecast.domain.model

import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
data class MindMapNode(
    val id: String = UUID.randomUUID().toString(),
    val label: String,
    val children: List<MindMapNode> = emptyList(),
    val colorHex: String? = null
)