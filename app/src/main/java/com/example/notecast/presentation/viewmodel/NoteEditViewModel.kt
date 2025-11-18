//package com.example.notecast.presentation.viewmodel
//
//import androidx.lifecycle.SavedStateHandle
//import androidx.lifecycle.ViewModel
//import androidx.lifecycle.viewModelScope
//import com.example.notecast.domain.model.Folder
//import com.example.notecast.domain.model.Note
//import com.example.notecast.domain.usecase.CreateNoteUseCase // SỬA
//import com.example.notecast.domain.usecase.GetNoteByIdUseCase // SỬA
//import dagger.hilt.android.lifecycle.HiltViewModel
//import kotlinx.coroutines.flow.MutableStateFlow
//import kotlinx.coroutines.flow.StateFlow
//import kotlinx.coroutines.flow.asStateFlow
//import kotlinx.coroutines.flow.update
//import kotlinx.coroutines.launch
//import java.util.Date
//import javax.inject.Inject
//
//@HiltViewModel
//class NoteEditViewModel @Inject constructor(
//    private val createNoteUseCase: CreateNoteUseCase, // SỬA
//    private val getNoteByIdUseCase: GetNoteByIdUseCase, // SỬA
//    savedStateHandle: SavedStateHandle
//) : ViewModel(), NoteEditViewModelContract { // SỬA: Implement Contract
//
//    private val _state = MutableStateFlow(NoteEditScreenState())
//    override val state: StateFlow<NoteEditScreenState> = _state.asStateFlow()
//
//    // SỬA: Lấy 'noteId' kiểu String (khớp với DAO/Domain mới)
//    private val noteId: String? = savedStateHandle.get<String>("noteId")
//
//    init {
//        // SỬA: ID "0" (String) nghĩa là Tạo mới
//        if (noteId != null && noteId != "0") {
//            loadNote(noteId)
//        } else {
//            // Tạo ghi chú mới
//            _state.update {
//                it.copy(note = Note(
//                    id = "0", // ID tạm
//                    title = "",
//                    content = "",
//                    updatedAt = System.currentTimeMillis()
//                ))
//            }
//        }
//    }
//
//    private fun loadNote(id: String) {
//        _state.update { it.copy(isLoading = true) }
//        viewModelScope.launch {
//            try {
//                val loadedNote = getNoteByIdUseCase(id)
//                _state.update {
//                    it.copy(note = loadedNote, isLoading = false)
//                }
//            } catch (e: Exception) {
//                _state.update { it.copy(isLoading = false, errorMessage = e.message) }
//            }
//        }
//    }
//
//    // --- Hành động người dùng (Implement Contract) ---
//
//    override fun onTitleChange(newTitle: String) {
//        _state.update { currentState ->
//            currentState.note?.let {
//                currentState.copy(note = it.copy(title = newTitle, updatedAt = Date().time))
//            } ?: currentState
//        }
//    }
//
//    override fun onContentChange(newContent: String) {
//        _state.update { currentState ->
//            currentState.note?.let {
//                currentState.copy(note = it.copy(content = newContent, updatedAt = Date().time))
//            } ?: currentState
//        }
//    }
//
//    override fun togglePin() {
//        // (Domain Model mới không có isPinned, bỏ qua)
//    }
//
//    override fun toggleFavorite() {
//        _state.update { currentState ->
//            currentState.note?.let {
//                currentState.copy(note = it.copy(isFavorite = !it.isFavorite, updatedAt = Date().time))
//            } ?: currentState
//        }
//    }
//
//    override fun saveNote() {
//        val currentNote = _state.value.note
//        if (currentNote != null) {
//            viewModelScope.launch {
//                try {
//                    // SỬA: Gọi CreateNoteUseCase (tự xử lý Cập nhật/Tạo mới)
//                    createNoteUseCase(currentNote)
//                    println("Note saved successfully!")
//                } catch (e: Exception) {
//                    _state.update { it.copy(errorMessage = e.message) }
//                }
//            }
//        }
//    }
//
//    // --- Hành động AI (Implement Contract) ---
//
//    override fun summarizeNote() {
//        _state.update { it.copy(isSummarizing = true) }
//        viewModelScope.launch {
//            kotlinx.coroutines.delay(2000)
//            val newContent = "Nội dung đã được tóm tắt (ngắn gọn hơn nhiều)."
//            _state.update { currentState ->
//                currentState.note?.let {
//                    currentState.copy(
//                        note = it.copy(content = newContent, updatedAt = Date().time),
//                        isSummarizing = false
//                    )
//                } ?: currentState.copy(isSummarizing = false)
//            }
//        }
//    }
//
//    override fun normalizeNote() {
//        println("Normalizing note...")
//    }
//
//    override fun generateMindMap() {
//        println("Generating Mind Map...")
//    }
//}