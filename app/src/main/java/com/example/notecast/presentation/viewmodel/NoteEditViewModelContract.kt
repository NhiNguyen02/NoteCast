package com.example.notecast.presentation.viewmodel

import com.example.notecast.domain.model.Note
import kotlinx.coroutines.flow.StateFlow

// SỬA: Định nghĩa State bên ngoài ViewModel
data class NoteEditScreenState(
    val note: Note? = null, // Note có thể null khi đang tải
    val isLoading: Boolean = false,
    val isSummarizing: Boolean = false,
    val errorMessage: String? = null
)

/**
 * Định nghĩa "hợp đồng" (Contract) cho NoteEditViewModel.
 * Cả ViewModel Hilt (thật) và ViewModel Mock (giả) sẽ implement interface này.
 */
interface NoteEditViewModelContract {
    val state: StateFlow<NoteEditScreenState>
    fun onTitleChange(newTitle: String)
    fun onContentChange(newContent: String)
    fun togglePin()
    fun toggleFavorite()
    fun saveNote()
    fun summarizeNote()
    fun normalizeNote()
    fun generateMindMap()
}