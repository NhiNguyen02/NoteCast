package com.example.notecast.presentation.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.notecast.domain.model.Note
import com.example.notecast.domain.usecase.GetAllFoldersUseCase
import com.example.notecast.domain.usecase.GetNoteByIdUseCase
import com.example.notecast.domain.usecase.SaveNoteUseCase
import com.example.notecast.presentation.ui.noteeditscreen.NoteEditEvent
import com.example.notecast.presentation.ui.noteeditscreen.NoteEditState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NoteEditViewModel @Inject constructor(
    private val getNoteByIdUseCase: GetNoteByIdUseCase,
    private val saveNoteUseCase: SaveNoteUseCase,
    private val getAllFoldersUseCase: GetAllFoldersUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _state = MutableStateFlow(NoteEditState())
    val state = _state.asStateFlow()

    private val noteId: String? = savedStateHandle["noteId"]
    private val initialContent: String? = savedStateHandle["initialContent"]
    private val audioPath: String? = savedStateHandle["audioPath"]
    private val durationMs: Long = savedStateHandle["durationMs"] ?: 0L
    private val sampleRate: Int = savedStateHandle["sampleRate"] ?: 0
    private val channels: Int = savedStateHandle["channels"] ?: 0

    init {
        loadFolders()
        when {
            noteId != null && noteId != "new" && noteId != "0" && noteId != "new_voice" -> {
                _state.update { it.copy(isLoading = true) }
                loadNote(noteId)
            }
            noteId == "new_voice" -> {
                // Khởi tạo màn Edit cho VOICE note mới với transcript ban đầu
                _state.update {
                    it.copy(
                        noteId = "",
                        noteType = "VOICE",
                        content = initialContent.orEmpty(),
                        audioFilePath = audioPath?.takeIf { it.isNotEmpty() },
                        audioDurationMs = durationMs.takeIf { it > 0L },
                        audioSampleRate = sampleRate.takeIf { it > 0 },
                        audioChannels = channels.takeIf { it > 0 },
                        isLoading = false,
                    )
                }
            }
        }
    }

    private fun loadFolders() {
        getAllFoldersUseCase().onEach { folders ->
            _state.update { it.copy(availableFolders = folders) }
            // Nếu đang có folderId, cập nhật lại folderName cho đúng
            val currentFolder = folders.find { f -> f.id == _state.value.folderId }
            if (currentFolder != null) {
                _state.update { it.copy(folderName = currentFolder.name) }
            }
        }.launchIn(viewModelScope)
    }
    private fun loadNote(id: String) {
        getNoteByIdUseCase(id).onEach { note ->
            if (note != null) {
                val folderName = _state.value.availableFolders.find { it.id == note.folderId }?.name ?: "Chưa phân loại"
                _state.update {
                    it.copy(
                        isLoading = false,
                        noteId = note.id,
                        title = note.title,
                        content = note.content ?: "",
                        noteType = note.noteType,
                        isFavorite = note.isFavorite, // Load trạng thái yêu thích
                        pinTimestamp = note.pinTimestamp,
                        updatedAt = note.updatedAt,
                        createdAt = note.createdAt,
                        folderId = note.folderId,
                        folderName = folderName,
                    )
                }
            } else {
                _state.update { it.copy(isLoading = false, error = "Không tìm thấy") }
            }
        }.launchIn(viewModelScope)
    }

    fun onEvent(event: NoteEditEvent) {
        when (event) {
            is NoteEditEvent.OnTitleChanged -> {
                _state.update { it.copy(title = event.title) }
            }
            is NoteEditEvent.OnContentChanged -> {
                _state.update { it.copy(content = event.content) }
            }
            is NoteEditEvent.OnToggleFavorite -> {
                _state.update { it.copy(isFavorite = !it.isFavorite) }
            }
            is NoteEditEvent.OnSaveNote -> {
                saveNote()
            }
            is NoteEditEvent.OnFolderSelected -> {
                val folderId = event.folder?.id
                val folderName = event.folder?.name ?: "Chưa phân loại"
                _state.update {
                    it.copy(
                        folderId = folderId,
                        folderName = folderName
                    )
                }
            }
            // --- XỬ LÝ AI (GIẢ LẬP) ---
            is NoteEditEvent.OnSummarize -> {
                simulateAiProcess(
                    onStart = { _state.update { it.copy(isSummarizing = true) } },
                    onEnd = { _state.update { it.copy(isSummarizing = false, content = it.content + "\n\n[Tóm tắt]: Nội dung đã được tóm tắt.") } }
                )
            }
            is NoteEditEvent.OnNormalize -> {
                simulateAiProcess(
                    onStart = { _state.update { it.copy(isNormalizing = true) } },
                    onEnd = { _state.update { it.copy(isNormalizing = false, content = it.content.trim()) } } // Ví dụ trim
                )
            }
            is NoteEditEvent.OnGenerateMindMap -> {
                simulateAiProcess(
                    onStart = { _state.update { it.copy(isGeneratingMindMap = true) } },
                    onEnd = { _state.update { it.copy(isGeneratingMindMap = false) } }
                )
            }
        }
    }

    // Hàm giả lập xử lý AI (delay 2 giây)
    private fun simulateAiProcess(onStart: () -> Unit, onEnd: () -> Unit) {
        viewModelScope.launch {
            onStart()
            delay(2000)
            onEnd()
        }
    }

    private fun saveNote() {
        viewModelScope.launch {
            val currentState = _state.value
            val now = System.currentTimeMillis()
            val createdAt = currentState.createdAt.takeIf { it != 0L } ?: now
            val note = Note(
                id = currentState.noteId ?: "",
                title = currentState.title,
                content = currentState.content,
                noteType = currentState.noteType,
                updatedAt = now,
                createdAt = createdAt,
                pinTimestamp = currentState.pinTimestamp,
                tags = emptyList(),
                isFavorite = currentState.isFavorite,
                folderId = currentState.folderId,
                // Audio metadata
                filePath = currentState.audioFilePath,
                durationMs = currentState.audioDurationMs,
            )
            saveNoteUseCase(note)
            _state.update { it.copy(isSaved = true) }
        }
    }
}