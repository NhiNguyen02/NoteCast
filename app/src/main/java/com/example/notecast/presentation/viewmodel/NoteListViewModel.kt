package com.example.notecast.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.notecast.domain.model.Note
import com.example.notecast.domain.usecase.notefolder.DeleteNoteUseCase
import com.example.notecast.domain.usecase.notefolder.GetAllFoldersUseCase
import com.example.notecast.domain.usecase.notefolder.GetAllNotesUseCase
import com.example.notecast.domain.usecase.notefolder.GetNotesByFolderUseCase
import com.example.notecast.domain.usecase.notefolder.SaveNoteUseCase
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
    private val deleteNoteUseCase: DeleteNoteUseCase,
    private val getNotesByFolderUseCase: GetNotesByFolderUseCase,
) : ViewModel() {

    private val _state = MutableStateFlow(NoteListState())
    val state: StateFlow<NoteListState> = _state.asStateFlow()

    init {
        // Tải 2 danh sách gốc (master lists)
        val notesFlow = getAllNotesUseCase()
        val foldersFlow = getAllFoldersUseCase()


        combine(notesFlow, foldersFlow, _state) { notes, folders, state ->

            val counts = FilterCounts(
                voiceCount = notes.count { it.noteType == "VOICE" },
                textCount = notes.count { it.noteType == "TEXT" },
                pinnedCount = notes.count { it.pinTimestamp != null },
                favoriteCount = notes.count { it.isFavorite },
                allFoldersCount = notes.size,
                folderCounts = notes.groupBy { it.folderId ?: "null" }
                    .mapValues { it.value.size }
            )
            // Cập nhật danh sách gốc
            var processingList = notes

            // Lọc (Filter)
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

                val folderMatch = if (state.filterOptions.folderId == null) {
                    true
                } else {
                    // Chỉ lấy note thuộc folder đang chọn
                    note.folderId == state.filterOptions.folderId
                }

                searchMatch && typeMatch && statusMatch && folderMatch
            }

            // Sắp xếp (Sort)
            processingList = when (state.sortOptions.sortBy) {
                SortBy.DATE_UPDATED -> {
                    if (state.sortOptions.direction == SortDirection.DESCENDING)
                        processingList.sortedByDescending { it.updatedAt }
                    else
                        processingList.sortedBy { it.updatedAt }
                }

                SortBy.DATE_CREATED -> {
                    if (state.sortOptions.direction == SortDirection.DESCENDING)
                        processingList.sortedByDescending { it.createdAt } // Mới tạo nhất lên đầu
                    else
                        processingList.sortedBy { it.createdAt }
                } // (Tương tự)
                SortBy.TITLE -> {
                    if (state.sortOptions.direction == SortDirection.DESCENDING)
                        processingList.sortedByDescending { it.title }
                    else
                        processingList.sortedBy { it.title }
                }
            } as List<Note>

            //  Trả về State cuối cùng
            state.copy(
                isLoading = false,
                allNotes = notes,
                filterCounts = counts,
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
                    val note = _state.value.allNotes.find { it.id == event.note.id }
                    if (note != null) {
                        saveNoteUseCase(note.copy(isFavorite = !note.isFavorite))
                    }
//                    saveNoteUseCase(event.note.copy(isFavorite = !event.note.isFavorite))
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
    fun deleteMultipleNotes(noteIds: List<String>) {
        viewModelScope.launch {
            noteIds.forEach { id ->
                deleteNoteUseCase(id)
            }
        }
    }
    fun moveNotesToFolder(noteIds: List<String>, targetFolderId: String?) {
        viewModelScope.launch {
            // Lọc lấy các note cần di chuyển từ list hiện tại
            val notesToMove = _state.value.allNotes.filter { it.id in noteIds }

            notesToMove.forEach { note ->
                // Cập nhật folderId mới
                val updatedNote = note.copy(folderId = targetFolderId, updatedAt = System.currentTimeMillis())
                saveNoteUseCase(updatedNote)
            }
        }
    }
}