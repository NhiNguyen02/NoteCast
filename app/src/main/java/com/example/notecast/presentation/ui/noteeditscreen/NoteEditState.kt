package com.example.notecast.presentation.ui.noteeditscreen

import com.example.notecast.domain.model.Folder
import com.example.notecast.domain.model.ProcessedTextData

/**
 * Trạng thái của màn hình Sửa/Tạo Ghi chú.
 */
data class NoteEditState(
    val isLoading: Boolean = false,
    val noteId: String? = null,

    // Dữ liệu ghi chú
    val title: String = "",
    val content: String = "",
    val noteType: String = "TEXT",
    val createdAt: Long = 0,
    val updatedAt: Long = System.currentTimeMillis(),
    val isFavorite: Boolean = false,
    val pinTimestamp: Long? = null,
    val folderId: String? = null, // ID của folder hiện tại (null = Chưa phân loại)
    val folderName: String = "Chưa phân loại", // Tên hiển thị trên Chip
    val availableFolders: List<Folder> = emptyList(), // Danh sách folder để chọn

    // Metadata audio cho VOICE note (có thể null đối với TEXT note)
    val audioFilePath: String? = null,
    val audioDurationMs: Long? = null,
    val audioSampleRate: Int? = null,
    val audioChannels: Int? = null,

    // Trạng thái xử lý AI (để hiện loading spinner trên chip)
    val isSummarizing: Boolean = false,
    val isNormalizing: Boolean = false,
    val isGeneratingMindMap: Boolean = false,
    val showMindMapDialog: Boolean = false,


    val processedTextData: ProcessedTextData? = null, // Lưu kết quả NLP (Keywords, Sentences) tạm thời
    val processingPercent: Int = 0,
    // Điều hướng
    val isSaved: Boolean = false,
    val error: String? = null
)