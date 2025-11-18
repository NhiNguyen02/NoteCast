package com.example.notecast.domain.model

import kotlinx.serialization.Serializable

/**
 * Domain Model chính, đại diện cho MỘT ghi chú.
 * Nó gộp tất cả thông tin từ 4 bảng (Note, Audio, Transcript, ProcessedText)
 * thành một đối tượng duy nhất.
 *
 * UI sẽ dùng 'noteType' để quyết định hiển thị gì.
 * Các trường như 'filePath', 'rawText'... là nullable vì Text Note sẽ không có.
 */
@Serializable
data class Note(
    // === Thông tin cơ bản (Từ NoteEntity) ===
    val id: String,
    val noteType: String, // "TEXT" hoặc "VOICE"
    val title: String,

    /**
     * - Đối với Text Note: Đây là nội dung người dùng gõ.
     * - Đối với Voice Note: Đây là nội dung người dùng đã chỉnh sửa.
     */
    val content: String? = null,
    val tags: List<String> = emptyList(),
    val mindMapJson: String? = null,
    val isFavorite: Boolean = false,
    val pinTimestamp: Long? = null, // Dùng cho tính năng Ghim
    val folderId: String? = null,
    val colorHex: String? = null,
    val updatedAt: Long,

    // === Thông tin Audio (Từ AudioEntity, nullable) ===
    val filePath: String? = null,       // local path
    val cloudUrl: String? = null,
    val durationMs: Long? = null,

    // === Thông tin Transcript (Từ TranscriptEntity, nullable) ===
    /**
     * Văn bản gốc 100% từ chuyển đổi.
     * Dùng cho Tab Âm thanh (bản chép lời).
     */
    val rawText: String? = null,
    val timestampsJson: String? = null, // Dữ liệu khớp audio/text

    // === Thông tin Đã xử lý (Từ ProcessedTextEntity, nullable) ===
    val punctuatedText: String? = null,
    val summary: String? = null,
    val sentiment: String? = null,

    // === Metadata đồng bộ (Từ tất cả Entities) ===
    val isSynced: Boolean = false,
    val isDeleted: Boolean = false
)