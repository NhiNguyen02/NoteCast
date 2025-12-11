package com.example.notecast.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.notecast.domain.model.Folder
import com.example.notecast.domain.model.Note
import com.example.notecast.domain.usecase.notefolder.DeleteFolderUseCase
import com.example.notecast.domain.usecase.notefolder.DeleteNoteUseCase
import com.example.notecast.domain.usecase.notefolder.GetAllFoldersUseCase
import com.example.notecast.domain.usecase.notefolder.GetAllNotesUseCase // <-- IMPORT MỚI
import com.example.notecast.domain.usecase.notefolder.GetNotesByFolderUseCase
import com.example.notecast.domain.usecase.notefolder.SaveFolderUseCase
import com.example.notecast.domain.usecase.notefolder.SaveNoteUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject


// Hằng số ID cho thư mục ảo
const val UNCATEGORIZED_FOLDER_ID = "uncategorized_virtual_id"
data class FolderState(
    val folders: List<Folder> = emptyList(),
    val folderNotes: List<Note> = emptyList(), // Notes của folder đang mở
    val noteCounts: Map<String, Int> = emptyMap(),
    val isLoading: Boolean = false
)

@HiltViewModel
class FolderViewModel @Inject constructor(
    private val getAllFoldersUseCase: GetAllFoldersUseCase,
    private val getAllNotesUseCase: GetAllNotesUseCase, // <-- INJECT MỚI
    private val saveFolderUseCase: SaveFolderUseCase,
    private val deleteFolderUseCase: DeleteFolderUseCase,
    private val getNotesByFolderUseCase: GetNotesByFolderUseCase,
    private val saveNoteUseCase: SaveNoteUseCase,
    private val deleteNoteUseCase: DeleteNoteUseCase,
) : ViewModel() {

    private val _state = MutableStateFlow(FolderState())
    val state: StateFlow<FolderState> = _state.asStateFlow()
    // Lưu giữ danh sách tất cả note để dùng cho việc lọc local
    private var cachedAllNotes: List<Note> = emptyList()
    private var getNotesJob: Job? = null

    init {
        loadData()
    }

    private fun loadData() {
        _state.update { it.copy(isLoading = true) }

        // Kết hợp luồng Folders và Notes để tính toán số lượng
        combine(
            getAllFoldersUseCase(),
            getAllNotesUseCase()
        ) { realFolders, allNotes ->
            cachedAllNotes = allNotes
            // 1. Tính toán số lượng cho các Folder thật
            val counts = allNotes.groupBy { it.folderId ?: "null" }
                .mapValues { entry -> entry.value.size }
                .toMutableMap() // Chuyển sang Mutable để thêm key ảo

            val uncategorizedCount = allNotes.count { it.folderId == null }
            val displayFolders = ArrayList<Folder>()

            // NẾU CÓ GHI CHÚ CHƯA PHÂN LOẠI -> THÊM THƯ MỤC ẢO VÀO ĐẦU
            if (uncategorizedCount > 0) {
                val virtualFolder = Folder(
                    id = UNCATEGORIZED_FOLDER_ID, // ID đặc biệt
                    name = "Chưa phân loại",
                    colorHex = "#CCA8FF", // Màu tím nhạt (giống Chip bạn thích)
                    createdAt = 0,
                    updatedAt = 0
                )
                displayFolders.add(virtualFolder)
                // Thêm count vào map để UI hiển thị đúng số lượng
                counts[UNCATEGORIZED_FOLDER_ID] = uncategorizedCount
            }

            displayFolders.addAll(realFolders)
            // Trả về bộ 3 dữ liệu
            Triple(displayFolders, counts, false)
            // Logic đếm: Gom nhóm note theo folderId và đếm kích thước
        }.onEach { (folders, counts) ->
            _state.update {
                it.copy(
                    folders = folders,
                    noteCounts = counts, // Cập nhật Map số lượng
                    isLoading = false
                )
            }
        }.launchIn(viewModelScope)
    }

    fun loadNotesByFolder(folderId: String) {
        if (folderId == UNCATEGORIZED_FOLDER_ID) {
            // Nếu là folder ảo -> Lọc lấy các note có folderId = null từ cache
            val uncategorizedNotes = cachedAllNotes.filter { it.folderId == null }
            _state.update { it.copy(folderNotes = uncategorizedNotes) }
        } else {
            // Nếu là folder thật -> Gọi UseCase
            getNotesByFolderUseCase(folderId).onEach { notes ->
                _state.update { it.copy(folderNotes = notes) }
            }.launchIn(viewModelScope)
        }
    }

    fun toggleFavorite(note: Note) {
        viewModelScope.launch {
            saveNoteUseCase(note.copy(isFavorite = !note.isFavorite))
        }
    }

    fun togglePin(note: Note) {
        viewModelScope.launch {
            val newPinTimestamp = if (note.pinTimestamp != null) null else System.currentTimeMillis()
            saveNoteUseCase(note.copy(pinTimestamp = newPinTimestamp))
        }
    }

    fun createFolder(folder: Folder) {
        viewModelScope.launch { saveFolderUseCase(folder) }
    }

    fun deleteFolder(id: String) {
        if (id == UNCATEGORIZED_FOLDER_ID) return
        viewModelScope.launch { deleteFolderUseCase(id) }
    }
    fun deleteMultipleNotes(noteIds: List<String>) {
        viewModelScope.launch {
            noteIds.forEach { id -> deleteNoteUseCase(id) }

        }
    }
//    Di chuyển nhiều ghi chú sang folder khác
    fun moveNotesToFolder(noteIds: List<String>, targetFolderId: String?) {
        viewModelScope.launch {
            // Lấy danh sách note hiện tại từ state
            val currentNotes = _state.value.folderNotes

            // Lọc ra các note cần di chuyển
            val notesToMove = currentNotes.filter { it.id in noteIds }

            notesToMove.forEach { note ->
                // Cập nhật folderId mới và thời gian sửa
                val updatedNote = note.copy(
                    folderId = targetFolderId,
                    updatedAt = System.currentTimeMillis()
                )
                saveNoteUseCase(updatedNote)
            }
        }
    }
}