package com.example.notecast.presentation.ui.homescreen

import com.example.notecast.domain.model.NoteDomain

/**
 * Event MỚI: Thêm các sự kiện Lọc & Sắp xếp
 */
sealed interface NoteListEvent {
    // Sự kiện từ Thanh tìm kiếm
    data class OnSearchQueryChanged(val query: String) : NoteListEvent

    // Sự kiện khi người dùng nhấn "Áp dụng" từ SortScreen
    data class OnApplySort(val sortOptions: SortOptions) : NoteListEvent

    // Sự kiện khi người dùng nhấn "Áp dụng" từ FilterScreen
    data class OnApplyFilters(val filterOptions: FilterOptions) : NoteListEvent

    // Sự kiện từ NoteItem
    data class OnDeleteNote(val noteId: String) : NoteListEvent
    data class OnToggleFavorite(val noteId: String) : NoteListEvent
    data class OnTogglePin(val noteId: String) : NoteListEvent
}