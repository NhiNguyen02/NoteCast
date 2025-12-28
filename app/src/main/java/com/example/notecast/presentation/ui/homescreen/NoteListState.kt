package com.example.notecast.presentation.ui.homescreen

import com.example.notecast.domain.model.Folder
import com.example.notecast.domain.model.NoteDomain

/**
 * State MỚI: Giữ cả ghi chú, thư mục, VÀ các tùy chọn Lọc/Sắp xếp
 */
data class NoteListState(
    val isLoading: Boolean = true,

    // 1. Dữ liệu gốc (Master List)
    val allNotes: List<NoteDomain> = emptyList(),
    val allFolders: List<Folder> = emptyList(),

    // 2. Các tùy chọn hiện tại
    val searchQuery: String = "",
    val sortOptions: SortOptions = SortOptions(),
    val filterOptions: FilterOptions = FilterOptions(),

    // 3. Danh sách đã xử lý (để cho UI hiển thị)
    val filteredAndSortedNotes: List<NoteDomain> = emptyList(),

    val error: String? = null,
    val filterCounts: FilterCounts = FilterCounts(),
)