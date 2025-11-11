package com.example.notecast.presentation.viewmodel

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.notecast.domain.model.Folder // SỬ DỤNG MODEL MỚI
import com.example.notecast.domain.model.Note // SỬ DỤNG MODEL MỚI
import com.example.notecast.domain.usecase.notes.SaveNoteUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Date
import javax.inject.Inject

// State của màn hình
data class NoteEditScreenState(
    val note: Note = Note(),
    val isLoading: Boolean = false,
    val isSummarizing: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class NoteEditViewModel @Inject constructor(
    private val saveNoteUseCase: SaveNoteUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _state = MutableStateFlow(NoteEditScreenState())
    val state: StateFlow<NoteEditScreenState> = _state.asStateFlow()

    // Lấy noteId từ Navigation Arguments
    // Note: Khi sử dụng Hilt, SavedStateHandle đã được tiêm tự động
    private val noteId: Int? = savedStateHandle.get<Int>("noteId")

    init {
        if (noteId != null && noteId != 0) {
            loadNote(noteId)
        } else {
            // Khởi tạo ghi chú mới với thư mục "Chưa phân loại"
            _state.update {
                it.copy(note = Note(folder = Folder(id = 0, name = "Chưa phân loại", noteCount = 0, color = Color.Transparent)))
            }
        }
    }

    private fun loadNote(id: Int) {
        _state.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            // TODO: Thay thế bằng Use Case getNoteById
            val loadedNote = Note(
                id = id,
                title = "Tiêu đề ghi chú đã tải",
                content = "Nội dung ghi chú đã được tải lên.",
                folder = Folder(id = 1, name = "Công việc", noteCount = 24, color = Color(0xFF7B68EE)),
                isPinned = true,
                isFavorite = false
            )
            _state.update {
                it.copy(note = loadedNote, isLoading = false)
            }
        }
    }

    // --- Hành động người dùng ---

    fun onTitleChange(newTitle: String) {
        _state.update {
            it.copy(note = it.note.copy(title = newTitle, lastEdited = Date()))
        }
    }

    fun onContentChange(newContent: String) {
        _state.update {
            it.copy(note = it.note.copy(content = newContent, lastEdited = Date()))
        }
    }

    fun togglePin() {
        _state.update {
            it.copy(note = it.note.copy(isPinned = !it.note.isPinned, lastEdited = Date()))
        }
    }

    fun toggleFavorite() {
        _state.update {
            it.copy(note = it.note.copy(isFavorite = !it.note.isFavorite, lastEdited = Date()))
        }
    }

    fun saveNote() {
        viewModelScope.launch {
            try {
                // Gọi Use Case để xử lý logic lưu
                saveNoteUseCase(_state.value.note)
                println("Note saved successfully!")
                // TODO: Điều hướng trở lại sau khi lưu thành công
            } catch (e: Exception) {
                _state.update {
                    it.copy(errorMessage = e.message)
                }
            }
        }
    }

    // --- Hành động AI ---

    fun summarizeNote() {
        _state.update { it.copy(isSummarizing = true) }
        viewModelScope.launch {
            kotlinx.coroutines.delay(2000)
            val newContent = "Nội dung đã được tóm tắt (ngắn gọn hơn nhiều)."
            _state.update { currentState ->
                currentState.copy(
                    note = currentState.note.copy(content = newContent, lastEdited = Date()),
                    isSummarizing = false
                )
            }
        }
    }

    fun normalizeNote() {
        println("Normalizing note...")
    }

    fun generateMindMap() {
        println("Generating Mind Map...")
    }
}