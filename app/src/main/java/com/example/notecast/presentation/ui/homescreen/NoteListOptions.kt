package com.example.notecast.presentation.ui.homescreen

/**
 * File này chứa các class đại diện cho các lựa chọn Lọc & Sắp xếp
 */

// === Dành cho Sắp xếp (SortScreen) ===
enum class SortBy {
    DATE_CREATED,
    DATE_UPDATED,
    TITLE
}

enum class SortDirection {
    ASCENDING, // Tăng dần (A-Z, Cũ nhất)
    DESCENDING // Giảm dần (Z-A, Mới nhất)
}

data class SortOptions(
    val sortBy: SortBy = SortBy.DATE_UPDATED,
    val direction: SortDirection = SortDirection.DESCENDING
)

// === Dành cho Lọc (FilterScreen) ===
enum class NoteTypeFilter {
    ALL,
    VOICE,
    TEXT
}

enum class StatusFilter {
    NONE,
    PINNED,
    FAVORITE
}

data class FilterOptions(
    val noteType: NoteTypeFilter = NoteTypeFilter.ALL,
    val folderId: String? = null, // null = Tất cả thư mục
    val status: StatusFilter = StatusFilter.NONE
)