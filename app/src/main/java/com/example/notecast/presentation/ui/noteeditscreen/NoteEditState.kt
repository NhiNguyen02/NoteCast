package com.example.notecast.presentation.ui.noteeditscreen

import com.example.notecast.domain.model.Folder

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

    // Trạng thái xử lý AI (để hiện loading spinner trên chip)
    val isSummarizing: Boolean = false,
    val isNormalizing: Boolean = false,
    val isGeneratingMindMap: Boolean = false,

    // Điều hướng
    val isSaved: Boolean = false,
    val error: String? = null
)