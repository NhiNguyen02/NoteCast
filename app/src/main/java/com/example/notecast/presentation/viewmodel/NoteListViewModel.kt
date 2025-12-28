package com.example.notecast.presentation.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.notecast.domain.model.NoteDomain
import com.example.notecast.domain.model.NoteType
import com.example.notecast.domain.usecase.notefolder.DeleteNoteUseCase
import com.example.notecast.domain.usecase.notefolder.GetAllFoldersUseCase
import com.example.notecast.domain.usecase.notefolder.GetAllNotesUseCase
import com.example.notecast.domain.usecase.notefolder.SaveNoteUseCase
import com.example.notecast.domain.usecase.notefolder.SyncNotesUseCase
import com.example.notecast.presentation.ui.homescreen.*
import com.example.notecast.presentation.ui.sort.SortBy
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.collections.filter

@HiltViewModel
class NoteListViewModel @Inject constructor(
    getAllNotesUseCase: GetAllNotesUseCase,
    getAllFoldersUseCase: GetAllFoldersUseCase,
    private val saveNoteUseCase: SaveNoteUseCase,
    private val deleteNoteUseCase: DeleteNoteUseCase,
    private val syncNotesUseCase: SyncNotesUseCase,
) : ViewModel() {

    private val _state = MutableStateFlow(NoteListState())
    val state: StateFlow<NoteListState> = _state.asStateFlow()

    private val syncErrorHandler = CoroutineExceptionHandler { _, _ ->
        // Ignore sync errors so Home UI still shows cached notes from Room.
    }

    init {
        // Đồng bộ notes từ NoteServices về Room khi ViewModel được tạo.
        viewModelScope.launch(syncErrorHandler) {
            runCatching { syncNotesUseCase() }
                .onFailure { throwable ->
                    Log.e("NoteListVM", "Sync failed", throwable)
                }
        }

        val notesFlow = getAllNotesUseCase()      // Flow<List<NoteDomain>>
        val foldersFlow = getAllFoldersUseCase()  // Flow<List<Folder>>

        combine(notesFlow, foldersFlow, _state) { notes, folders, state ->

            val counts = FilterCounts(
                voiceCount = notes.count { it.type == NoteType.AUDIO },
                textCount = notes.count { it.type == NoteType.TEXT },
                pinnedCount = notes.count { it.isPinned },
                favoriteCount = notes.count { it.isFavorite },
                allFoldersCount = notes.size,
                folderCounts = notes.groupBy { it.folderId ?: "null" }
                    .mapValues { it.value.size }
            )

            var processingList: List<NoteDomain> = notes

            // Lọc (Filter)
            processingList = processingList.filter { note ->
                // Tìm kiếm theo title / rawText / normalizedText
                val searchMatch = if (state.searchQuery.isNotBlank()) {
                    val q = state.searchQuery
                    (note.title?.contains(q, ignoreCase = true) == true) ||
                            (note.rawText?.contains(q, ignoreCase = true) == true) ||
                            (note.normalizedText?.contains(q, ignoreCase = true) == true)
                } else true

                // Lọc theo Loại (Type)
                val typeMatch = when (state.filterOptions.noteType) {
                    NoteTypeFilter.ALL -> true
                    NoteTypeFilter.VOICE -> note.type == NoteType.AUDIO
                    NoteTypeFilter.TEXT -> note.type == NoteType.TEXT
                }

                // Lọc theo Trạng thái (Status)
                val statusMatch = when (state.filterOptions.status) {
                    StatusFilter.NONE -> true
                    StatusFilter.PINNED -> note.isPinned
                    StatusFilter.FAVORITE -> note.isFavorite
                }

                val folderMatch = state.filterOptions.folderId?.let { fid ->
                    note.folderId == fid
                } ?: true

                searchMatch && typeMatch && statusMatch && folderMatch
            }

            // ===== SẮP XẾP (SORT) =====
            // 1️⃣ Sort theo rule người dùng chọn
            val sortedByRule: List<NoteDomain> = when (state.sortOptions.sortBy) {

                SortBy.DATE_UPDATED -> {
                    if (state.sortOptions.direction == SortDirection.DESCENDING)
                        processingList.sortedByDescending { it.updatedAt }
                    else
                        processingList.sortedBy { it.updatedAt }
                }

                SortBy.DATE_CREATED -> {
                    if (state.sortOptions.direction == SortDirection.DESCENDING)
                        processingList.sortedByDescending { it.createdAt }
                    else
                        processingList.sortedBy { it.createdAt }
                }

                SortBy.TITLE -> {
                    if (state.sortOptions.direction == SortDirection.DESCENDING)
                        processingList.sortedByDescending { it.title ?: "" }
                    else
                        processingList.sortedBy { it.title ?: "" }
                }

                SortBy.FAVORITE_FIRST -> {
                    processingList.sortedWith(
                        compareByDescending<NoteDomain> { it.isFavorite }
                            .thenByDescending { it.updatedAt }
                    )
                }

                // PINNED_FIRST chỉ ảnh hưởng thứ tự BÊN TRONG,
                // pin toàn cục sẽ xử lý ở bước 2
                SortBy.PINNED_FIRST -> processingList
            }

            // 2️⃣ LUÔN ưu tiên PIN (bất kể sort gì)
            processingList = sortedByRule.sortedWith(
                compareByDescending<NoteDomain> { it.isPinned }
                    .thenByDescending { it.updatedAt }
            )

            state.copy(
                isLoading = false,
                allNotes = notes,
                filterCounts = counts,
                allFolders = folders,
                filteredAndSortedNotes = processingList,
            )

        }.onEach { newState ->
            Log.d(
                "NoteListVM",
                "Home state updated: allNotes=${newState.allNotes.size}, " +
                    "filtered=${newState.filteredAndSortedNotes.size}, " +
                    "search='${newState.searchQuery}', " +
                    "filterType=${newState.filterOptions.noteType}, " +
                    "filterFolder=${newState.filterOptions.folderId}"
            )
            _state.value = newState
        }.launchIn(viewModelScope)
    }

    fun onEvent(event: NoteListEvent) {
        when (event) {
            is NoteListEvent.OnSearchQueryChanged -> {
                _state.update { it.copy(searchQuery = event.query) }
            }
            is NoteListEvent.OnApplyFilters -> {
                _state.update { it.copy(filterOptions = event.filterOptions) }
            }
            is NoteListEvent.OnApplySort -> {
                _state.update { it.copy(sortOptions = event.sortOptions) }
            }

            is NoteListEvent.OnDeleteNote -> {
                viewModelScope.launch { deleteNoteUseCase(event.noteId) }
            }
            is NoteListEvent.OnToggleFavorite -> {
                viewModelScope.launch {
                    val currentNote = _state.value.allNotes
                        .firstOrNull { it.id == event.noteId }
                        ?: return@launch
                    val updatedNote = currentNote.copy(
                        isFavorite = !currentNote.isFavorite
                    )
                    saveNoteUseCase(updatedNote)
                }
            }
            is NoteListEvent.OnTogglePin -> {
                viewModelScope.launch {
                    viewModelScope.launch {
                        val currentNote = _state.value.allNotes
                            .firstOrNull { it.id == event.noteId }
                            ?: return@launch

                        val updatedNote = currentNote.copy(
                            isPinned = !currentNote.isPinned
                        )
                        saveNoteUseCase(updatedNote)
                    }
                }
            }
        }
    }

    fun deleteMultipleNotes(noteIds: List<String>) {
        viewModelScope.launch {
            noteIds.forEach { id -> deleteNoteUseCase(id) }
        }
    }

    fun moveNotesToFolder(noteIds: List<String>, targetFolderId: String?) {
        viewModelScope.launch {
            val currentNotes: List<NoteDomain> = _state.value.allNotes
            for (note in currentNotes) {
                if (noteIds.contains(note.id)) {
                    val updatedNote = note.copy(folderId = targetFolderId)
                    saveNoteUseCase(updatedNote)
                }
            }
        }
    }
}