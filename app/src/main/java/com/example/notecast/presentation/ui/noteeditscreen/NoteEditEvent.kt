package com.example.notecast.presentation.ui.noteeditscreen

import com.example.notecast.domain.model.Folder

/**
 * Các sự kiện từ UI gửi lên ViewModel
 */
sealed interface NoteEditEvent {
    // Nhập liệu
    data class OnTitleChanged(val title: String) : NoteEditEvent
    data class OnContentChanged(val content: String) : NoteEditEvent

    // Hành động chính
    object OnSaveNote : NoteEditEvent
    object OnToggleFavorite : NoteEditEvent


    data class OnFolderSelected(val folder: Folder?) : NoteEditEvent
    // Hành động AI (Placeholder cho tính năng sau này)
    object OnSummarize : NoteEditEvent
    object OnNormalize : NoteEditEvent
    object OnGenerateMindMap : NoteEditEvent
}