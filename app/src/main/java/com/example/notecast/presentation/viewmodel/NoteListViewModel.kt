package com.example.notecast.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.notecast.domain.model.Note
import com.example.notecast.domain.usecase.DeleteNoteUseCase
import com.example.notecast.domain.usecase.GetAllFoldersUseCase
import com.example.notecast.domain.usecase.GetAllNotesUseCase
import com.example.notecast.domain.usecase.SaveNoteUseCase
import com.example.notecast.presentation.ui.homescreen.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NoteListViewModel @Inject constructor(
    getAllNotesUseCase: GetAllNotesUseCase,
    getAllFoldersUseCase: GetAllFoldersUseCase,
    private val saveNoteUseCase: SaveNoteUseCase,
    private val deleteNoteUseCase: DeleteNoteUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(NoteListState())
    val state: StateFlow<NoteListState> = _state.asStateFlow()

    init {
        // Tải 2 danh sách gốc (master lists)
        val notesFlow = getAllNotesUseCase()
        val foldersFlow = getAllFoldersUseCase()

        // "Cỗ máy" xử lý:
        // Bất cứ khi nào 1 trong 3 (notes, folders, state) thay đổi,
        // nó sẽ tự động chạy lại để tính toán danh sách cuối cùng
        combine(notesFlow, foldersFlow, _state) { notes, folders, state ->

            // 1. Cập nhật danh sách gốc
            var processingList = notes

            // 2. Lọc (Filter)
            processingList = processingList.filter { note ->
                // Lọc theo Tìm kiếm
                val searchMatch = if (state.searchQuery.isNotBlank()) {
                    note.title.contains(state.searchQuery, ignoreCase = true) ||
                            (note.content?.contains(state.searchQuery, ignoreCase = true) ?: false)
                } else true

                // Lọc theo Loại (Type)
                val typeMatch = when (state.filterOptions.noteType) {
                    NoteTypeFilter.ALL -> true
                    NoteTypeFilter.VOICE -> note.noteType == "VOICE"
                    NoteTypeFilter.TEXT -> note.noteType == "TEXT"
                }

                // Lọc theo Trạng thái (Status)
                val statusMatch = when (state.filterOptions.status) {
                    StatusFilter.NONE -> true
                    StatusFilter.PINNED -> note.pinTimestamp != null
                    StatusFilter.FAVORITE -> note.isFavorite
                }

                // (Bạn có thể thêm lọc theo FolderId ở đây)

                searchMatch && typeMatch && statusMatch
            }

            // 3. Sắp xếp (Sort)
            processingList = when (state.sortOptions.sortBy) {
                SortBy.DATE_UPDATED -> {
                    if (state.sortOptions.direction == SortDirection.DESCENDING)
                        processingList.sortedByDescending { it.updatedAt }
                    else
                        processingList.sortedBy { it.updatedAt }
                }

                SortBy.DATE_CREATED -> { /* ... */ } // (Tương tự)
                SortBy.TITLE -> {
                    if (state.sortOptions.direction == SortDirection.DESCENDING)
                        processingList.sortedByDescending { it.title }
                    else
                        processingList.sortedBy { it.title }
                }
            } as List<Note>

            // 4. Trả về State cuối cùng
            state.copy(
                isLoading = false,
                allNotes = notes,
                allFolders = folders,
                filteredAndSortedNotes = processingList // Đây là danh sách UI sẽ thấy
            )

        }.onEach { newState ->
            _state.value = newState // Cập nhật state cuối cùng
        }.launchIn(viewModelScope)
    }

    /**
     * Hàm nhận Event từ UI
     */
    fun onEvent(event: NoteListEvent) {
        when (event) {
            // Cập nhật các lựa chọn Lọc/Sắp xếp
            is NoteListEvent.OnSearchQueryChanged -> {
                _state.update { it.copy(searchQuery = event.query) }
            }
            is NoteListEvent.OnApplyFilters -> {
                _state.update { it.copy(filterOptions = event.filterOptions) }
            }
            is NoteListEvent.OnApplySort -> {
                _state.update { it.copy(sortOptions = event.sortOptions) }
            }

            // Các event Xóa/Ghim/Yêu thích
            is NoteListEvent.OnDeleteNote -> {
                viewModelScope.launch { deleteNoteUseCase(event.noteId) }
            }
            is NoteListEvent.OnToggleFavorite -> {
                viewModelScope.launch {
                    saveNoteUseCase(event.note.copy(isFavorite = !event.note.isFavorite))
                }
            }
            is NoteListEvent.OnTogglePin -> {
                viewModelScope.launch {
                    val note = event.note.copy(
                        pinTimestamp = if (event.note.pinTimestamp != null) null
                        else System.currentTimeMillis()
                    )
                    saveNoteUseCase(note)
                }
            }
        }
    }
}