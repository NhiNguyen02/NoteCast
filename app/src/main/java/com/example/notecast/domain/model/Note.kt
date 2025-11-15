package com.example.notecast.domain.model

import kotlinx.serialization.Serializable

/**
 * Domain model for Note.
 * - tags: list of strings (user-facing)
 * - mindMapJson: optional JSON structure representing mindmap (string form)
 * - processedTextId: reference to processed text (or you can embed processedText)
 */
@Serializable
data class Note(
    val id: String,
    val title: String,
    val content: String? = null,          // optional short content / preview
    val processedTextId: String? = null,
    val processedText: ProcessedText? = null, // optional embedded if available
    val tags: List<String> = emptyList(),
    val mindMapJson: String? = null,
    val isFavorite: Boolean = false,
    val folderId: String? = null,
    val colorHex: String? = null,
    val updatedAt: Long,
    val isSynced: Boolean = false,
    val isDeleted: Boolean = false
)