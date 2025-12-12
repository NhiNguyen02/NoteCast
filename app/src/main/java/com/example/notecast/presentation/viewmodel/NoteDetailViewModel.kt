package com.example.notecast.presentation.viewmodel

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.notecast.domain.model.ChunkResult
import com.example.notecast.domain.model.Folder
import com.example.notecast.domain.model.MindMapNode
import com.example.notecast.domain.model.Note
import com.example.notecast.domain.usecase.notefolder.GetAllFoldersUseCase
import com.example.notecast.domain.usecase.notefolder.GetNoteByIdUseCase
import com.example.notecast.domain.usecase.notefolder.SaveNoteUseCase
import com.example.notecast.domain.usecase.postprocess.GenerateMindMapUseCase
import com.example.notecast.presentation.navigation.Screen
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import java.net.URLDecoder
import java.util.UUID
import javax.inject.Inject

private const val TAG_NOTE_DETAIL = "NoteDetailVM"

/**
 * ViewModel cho NoteDetailTextScreen
 * - Nhận transcript từ nav args (title/date/content)
 * - Cho phép chuẩn hóa, tóm tắt, tạo mindmap
 * - Có thể lưu note xuống DB nếu cần (tối thiểu cho pipeline HF Space)
 */
@HiltViewModel
class NoteDetailViewModel @Inject constructor(
    private val saveNoteUseCase: SaveNoteUseCase,
    private val generateMindMapUseCase: GenerateMindMapUseCase,
    private val getAllFoldersUseCase: GetAllFoldersUseCase,
    private val getNoteByIdUseCase: GetNoteByIdUseCase,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    data class UiState(
        val noteId: String? = null,
        val title: String = "",
        val date: String = "",
        val content: String = "",
        val chunks: List<ChunkResult> = emptyList(),
        val noteType: String = "VOICE",
        val createdAt: Long = 0L,
        val updatedAt: Long = System.currentTimeMillis(),
        val isFavorite: Boolean = false,
        val pinTimestamp: Long? = null,
        val folderId: String? = null,
        val folderName: String = "Chưa phân loại",
        val availableFolders: List<Folder> = emptyList(),
        // Mindmap
        val isNormalizing: Boolean = false,
        val isSummarizing: Boolean = false,
        val isGeneratingMindMap: Boolean = false,
        val processingPercent: Int = 0,
        val mindMap: MindMapNode? = null,
        val showMindMapDialog: Boolean = false,
        // Trạng thái lỗi
        val error: String? = null,
    )

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    private val json = Json { ignoreUnknownKeys = true; encodeDefaults = true }

    init {
        val rawNoteId: String = savedStateHandle[Screen.NoteDetail.noteIdArg] ?: ""
        val rawTitle: String = savedStateHandle[Screen.NoteDetail.titleArg] ?: "Ghi chú ghi âm"
        val rawDate: String = savedStateHandle[Screen.NoteDetail.dateArg] ?: ""
        val rawContent: String = savedStateHandle[Screen.NoteDetail.contentArg] ?: ""
        val rawChunksJson: String = savedStateHandle[Screen.NoteDetail.chunksArg] ?: ""

        Log.d(TAG_NOTE_DETAIL, "Nav args: noteId='${rawNoteId}', title='${rawTitle}', date='${rawDate}', contentLength=${rawContent.length}, chunksJsonLength=${rawChunksJson.length}")

        val noteIdArg = rawNoteId.takeIf { it.isNotBlank() }
        val titleArg = try { URLDecoder.decode(rawTitle, "UTF-8") } catch (e: Exception) {
            Log.e(TAG_NOTE_DETAIL, "Failed to decode title: ${e.message}", e)
            rawTitle
        }
        val dateArg = try { URLDecoder.decode(rawDate, "UTF-8") } catch (e: Exception) {
            Log.e(TAG_NOTE_DETAIL, "Failed to decode date: ${e.message}", e)
            rawDate
        }
        val contentArg = try { URLDecoder.decode(rawContent, "UTF-8") } catch (e: Exception) {
            Log.e(TAG_NOTE_DETAIL, "Failed to decode content: ${e.message}", e)
            rawContent
        }
        val chunksJsonEncoded = try { URLDecoder.decode(rawChunksJson, "UTF-8") } catch (e: Exception) {
            Log.e(TAG_NOTE_DETAIL, "Failed to decode chunksJson: ${e.message}", e)
            rawChunksJson
        }

        val chunks = if (chunksJsonEncoded.isNotBlank()) {
            try {
                val list = json.decodeFromString<List<ChunkResult>>(chunksJsonEncoded)
                Log.d(TAG_NOTE_DETAIL, "Decoded chunks from nav args: count=${list.size}")
                list
            } catch (e: Exception) {
                Log.e(TAG_NOTE_DETAIL, "Failed to parse chunksJson: ${e.message}", e)
                emptyList()
            }
        } else {
            Log.d(TAG_NOTE_DETAIL, "chunksJsonEncoded is blank, no chunks provided")
            emptyList()
        }

        val inferredType = "VOICE"

        _uiState.update {
            it.copy(
                noteId = noteIdArg,
                title = titleArg,
                date = dateArg,
                content = contentArg,
                chunks = chunks,
                noteType = inferredType,
            )
        }

        loadFolders()

        if (noteIdArg != null) {
            getNoteByIdUseCase(noteIdArg).onEach { note ->
                if (note != null) {
                    val savedMindMap = if (!note.mindMapJson.isNullOrBlank()) {
                        try {
                            json.decodeFromString<MindMapNode>(note.mindMapJson)
                        } catch (_: Exception) { null }
                    } else null

                    _uiState.update { state ->
                        state.copy(
                            // giữ lại title/content từ nav nếu bạn muốn ưu tiên UI hiện tại,
                            // hoặc override bằng note.title/note.content nếu muốn sync tuyệt đối
                            title = state.title.ifBlank { note.title },
                            content = state.content.ifBlank { note.content.orEmpty() },

                            noteId = note.id,
                            noteType = note.noteType,
                            createdAt = note.createdAt,
                            updatedAt = note.updatedAt,
                            isFavorite = note.isFavorite,
                            pinTimestamp = note.pinTimestamp,
                            folderId = note.folderId,
                            mindMap = savedMindMap ?: state.mindMap,
                        )
                    }
                }
            }.launchIn(viewModelScope)
        }
    }

    private fun loadFolders() {
        getAllFoldersUseCase().onEach { folders ->
            _uiState.update { state ->
                val currentFolderId = state.folderId
                val folderName = folders.find { it.id == currentFolderId }?.name ?: state.folderName
                state.copy(
                    availableFolders = folders,
                    folderName = folderName,
                )
            }
        }.launchIn(viewModelScope)
    }

    fun onFolderSelected(folder: Folder?) {
        val folderId = folder?.id
        val folderName = folder?.name ?: "Chưa phân loại"
        _uiState.update {
            it.copy(
                folderId = folderId,
                folderName = folderName,
            )
        }
    }

    fun onTitleChanged(newTitle: String) {
        _uiState.update { it.copy(title = newTitle, mindMap = null) }
    }

    fun onContentChanged(newContent: String) {
        _uiState.update { it.copy(content = newContent, mindMap = null) }
    }

    fun onToggleFavorite() {
        _uiState.update { it.copy(isFavorite = !it.isFavorite) }
    }

    fun onNormalizeClicked() {
        val current = _uiState.value
        if (current.content.isBlank() || current.isNormalizing) return
        viewModelScope.launch {
            _uiState.update { it.copy(isNormalizing = true, error = null) }
            try {
                val normalized = current.content.trim()
                _uiState.update { it.copy(content = normalized, isNormalizing = false) }
            } catch (t: Throwable) {
                _uiState.update { it.copy(isNormalizing = false, error = t.message) }
            }
        }
    }

    fun onSummarizeClicked() {
        val current = _uiState.value
        if (current.content.isBlank() || current.isSummarizing) return
        viewModelScope.launch {
            _uiState.update { it.copy(isSummarizing = true, error = null) }
            try {
                val summary = current.content + "\n\n[Tóm tắt]: Nội dung đã được tóm tắt."
                _uiState.update { it.copy(content = summary, isSummarizing = false) }
            } catch (t: Throwable) {
                _uiState.update { it.copy(isSummarizing = false, error = t.message) }
            }
        }
    }

    fun onGenerateMindMapClicked() {
        val current = _uiState.value
        if (current.content.isBlank() || current.isGeneratingMindMap) return

        if (current.mindMap != null) {
            _uiState.update { it.copy(showMindMapDialog = true) }
            return
        }

        val ensuredId = current.noteId ?: UUID.randomUUID().toString()
        val nowTs = System.currentTimeMillis()
        val createdAt = if (current.createdAt != 0L) current.createdAt else nowTs

        val baseNote = Note(
            id = ensuredId,
            title = current.title,
            content = current.content,
            noteType = current.noteType,
            createdAt = createdAt,
            updatedAt = nowTs,
            isFavorite = current.isFavorite,
            pinTimestamp = current.pinTimestamp,
            folderId = current.folderId,
        )

        _uiState.update {
            it.copy(
                noteId = ensuredId,
                createdAt = createdAt,
                updatedAt = nowTs,
                isGeneratingMindMap = true,
                processingPercent = 0,
                error = null,
            )
        }

        viewModelScope.launch {
            val progressJob = launch {
                var p = 0
                while (p < 85) {
                    delay(200)
                    p += (2..5).random()
                    _uiState.update { it.copy(processingPercent = p.coerceAtMost(85)) }
                }
            }

            try {
                val rootNode = generateMindMapUseCase(baseNote)

                progressJob.cancel()
                _uiState.update { it.copy(processingPercent = 90) }

                val mindMapJson = json.encodeToString(MindMapNode.serializer(), rootNode)
                val noteToSave = baseNote.copy(mindMapJson = mindMapJson)
                saveNoteUseCase(noteToSave)

                _uiState.update { it.copy(processingPercent = 100) }
                delay(300)

                _uiState.update {
                    it.copy(
                        isGeneratingMindMap = false,
                        mindMap = rootNode,
                        showMindMapDialog = true,
                    )
                }
            } catch (t: Throwable) {
                progressJob.cancel()
                _uiState.update {
                    it.copy(
                        isGeneratingMindMap = false,
                        error = "Lỗi tạo Mindmap: ${t.message}",
                    )
                }
            }
        }
    }

    fun onCloseMindMapDialog() {
        _uiState.update { it.copy(showMindMapDialog = false) }
    }

    fun onSaveNote() {
        val current = _uiState.value
        viewModelScope.launch {
            try {
                val now = System.currentTimeMillis()
                val createdAt = current.createdAt.takeIf { it != 0L } ?: now
                val noteId = current.noteId ?: UUID.randomUUID().toString()
                val mindMapJson = current.mindMap?.let { json.encodeToString(MindMapNode.serializer(), it) }

                Log.d(
                    TAG_NOTE_DETAIL,
                    "Saving note: id=$noteId, type=${current.noteType}, title='${current.title}', contentLength=${current.content.length}, chunksCount=${current.chunks.size}"
                )

                val note = Note(
                    id = noteId,
                    title = current.title,
                    content = current.content,
                    noteType = current.noteType,
                    createdAt = createdAt,
                    updatedAt = now,
                    isFavorite = current.isFavorite,
                    pinTimestamp = current.pinTimestamp,
                    folderId = current.folderId,
                    mindMapJson = mindMapJson,
                )
                saveNoteUseCase(note)
                _uiState.update { it.copy(noteId = noteId) }
                Log.d(TAG_NOTE_DETAIL, "Note saved successfully: id=$noteId")
            } catch (t: Throwable) {
                Log.e(TAG_NOTE_DETAIL, "Error saving note: ${t.message}", t)
                _uiState.update { it.copy(error = t.message) }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}
