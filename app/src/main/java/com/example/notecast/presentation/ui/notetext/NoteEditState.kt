package com.example.notecast.presentation.ui.notetext

import com.example.notecast.domain.model.Folder
import com.example.notecast.domain.model.MindMapNode

/**
 * Trạng thái tối giản cho màn hình Sửa/Tạo ghi chú text.
 */
data class NoteEditState(
    val isLoading: Boolean = false,
    val noteId: String? = null,

    // Dữ liệu ghi chú cơ bản
    val title: String = "",
    val content: String = "",
    val createdAt: Long = 0L,
    val updatedAt: Long = System.currentTimeMillis(),

    // Folder
    val folderId: String? = null,
    val folderName: String = "Chưa phân loại",
    val availableFolders: List<Folder> = emptyList(),

    // Enrichment kết quả (được reflect từ NoteDomain sau Regenerate)
    val summary: String? = null,
    val keywords: List<String> = emptyList(),
    val mindMapData: MindMapNode? = null,

    // Trạng thái xử lý enrichment (UI feedback)
    val isSummarizing: Boolean = false,
    val isNormalizing: Boolean = false,
    val isGeneratingMindMap: Boolean = false,

    // Dialogs & lỗi
    val showMindMapDialog: Boolean = false,
    val isSaved: Boolean = false,
    val showSavedDialog: Boolean = false,
    val error: String? = null,
)