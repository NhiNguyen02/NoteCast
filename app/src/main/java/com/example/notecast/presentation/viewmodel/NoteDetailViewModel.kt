package com.example.notecast.presentation.viewmodel

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.notecast.domain.model.ChunkResult
import com.example.notecast.domain.model.Folder
import com.example.notecast.domain.model.MindMapNode
import com.example.notecast.domain.model.Note
import com.example.notecast.domain.model.ProcessedTextData
import com.example.notecast.domain.usecase.notefolder.GetAllFoldersUseCase
import com.example.notecast.domain.usecase.notefolder.GetNoteByIdUseCase
import com.example.notecast.domain.usecase.notefolder.SaveNoteUseCase
import com.example.notecast.domain.usecase.postprocess.GenerateMindMapUseCase
import com.example.notecast.domain.usecase.postprocess.NormalizationResult
import com.example.notecast.domain.usecase.postprocess.NormalizeNoteUseCase
import com.example.notecast.domain.usecase.postprocess.SummarizeNoteUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import java.util.UUID
import javax.inject.Inject


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
    private val normalizeNoteUseCase: NormalizeNoteUseCase,
    getNoteByIdUseCase: GetNoteByIdUseCase,
    savedStateHandle: SavedStateHandle,
    private val summarizeNoteUseCase: SummarizeNoteUseCase,
    ) : ViewModel() {
    private val TAG_NOTE_DETAIL = "NoteDetailVM"

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
        // Audio info
        val filePath: String? = null,
        val durationMs: Long? = null,
        // Transcript gốc (raw)
        val rawText: String? = null,
        val timestampsJson: String? = null,
        // Mindmap
        val isNormalizing: Boolean = false,
        val isSummarizing: Boolean = false,
        val isGeneratingMindMap: Boolean = false,
        val processingPercent: Int = 0,
        // New: distinguish which process is active if needed
        val activeProcess: ActiveProcess? = null,
        val mindMap: MindMapNode? = null,
        val processedTextData: ProcessedTextData? = null,
        val showMindMapDialog: Boolean = false,
        // Trạng thái lỗi
        val error: String? = null,
    )

    // Identify current long-running process for better UI mapping
    enum class ActiveProcess { NORMALIZE, SUMMARIZE, MINDMAP }

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    private val json = Json { ignoreUnknownKeys = true; encodeDefaults = true }

    // progress jobs
    private var summarizeProgressJob: Job? = null

    init {
        // Lấy noteId từ route NoteDetailText: "note_detail_text/{noteId}"
        val noteIdArg: String? = savedStateHandle["noteId"]
        Log.d(TAG_NOTE_DETAIL, "Init NoteDetailViewModel with noteId='$noteIdArg'")

        if (noteIdArg != null) {
            getNoteByIdUseCase(noteIdArg).onEach { note ->
                if (note != null) {
                    val savedMindMap = if (!note.mindMapJson.isNullOrBlank()) {
                        try {
                            json.decodeFromString<MindMapNode>(note.mindMapJson)
                        } catch (_: Exception) { null }
                    } else null

                    // Parse timestampsJson -> chunks
                    val parsedChunks: List<ChunkResult> = note.timestampsJson
                        ?.let { rawJson ->
                            try {
                                json.decodeFromString<List<ChunkResult>>(rawJson)
                            } catch (_: Exception) {
                                emptyList()
                            }
                        } ?: emptyList()

                    _uiState.update { state ->
                        state.copy(
                            title = note.title,
                            content = note.content.orEmpty(),
                            noteId = note.id,
                            noteType = note.noteType,
                            createdAt = note.createdAt,
                            updatedAt = note.updatedAt,
                            isFavorite = note.isFavorite,
                            pinTimestamp = note.pinTimestamp,
                            folderId = note.folderId,
                            mindMap = savedMindMap ?: state.mindMap,
                            filePath = note.filePath,
                            durationMs = note.durationMs,
                            rawText = note.rawText,
                            timestampsJson = note.timestampsJson,
                            chunks = parsedChunks,
                        )
                    }
                }
            }.launchIn(viewModelScope)
        }

        loadFolders()
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
        val originalContent = _uiState.value.content
        if (originalContent.isBlank()) return

        viewModelScope.launch {
            // optional: you can also add a separate progress job like mind map if needed later
            _uiState.update { it.copy(isNormalizing = true, activeProcess = ActiveProcess.NORMALIZE, error = null) }

            normalizeNoteUseCase(originalContent).collect { result ->
                when (result) {
                    // 1. Giai đoạn Preview (Heuristic)
                    is NormalizationResult.Preview -> {
                        _uiState.update {
                            it.copy(
                                content = result.text,
                                isNormalizing = true,
                                activeProcess = ActiveProcess.NORMALIZE
                            )
                        }
                    }
                    // 2. Giai đoạn Success (AI trả về Object Data)
                    is NormalizationResult.Success -> {
                        val aiData = result.data
                        _uiState.update {
                            it.copy(
                                content = aiData.normalizedText,
                                processedTextData = aiData,
                                isNormalizing = false,
                                activeProcess = null,
                                mindMap = null
                            )
                        }
                    }
                    // 3. Giai đoạn Lỗi
                    is NormalizationResult.Error -> {
                        _uiState.update {
                            it.copy(
                                isNormalizing = false,
                                activeProcess = null,
                                error = "Lỗi chuẩn hóa, dùng bản nháp.",
                                content = result.text
                            )
                        }
                    }
                }
            }
        }
    }

    fun onSummarizeClicked() {
        if (_uiState.value.isSummarizing) return

        // start a progress job similar to mind map: ramp 0..85 until real result
        summarizeProgressJob?.cancel()

        viewModelScope.launch {
            _uiState.update { it.copy(isSummarizing = true, activeProcess = ActiveProcess.SUMMARIZE, processingPercent = 0, error = null) }

            summarizeProgressJob = launch {
                var p = 0
                while (p < 85 && _uiState.value.isSummarizing) {
                    delay(180)
                    p += (3..7).random()
                    _uiState.update { it.copy(processingPercent = p.coerceAtMost(85)) }
                }
            }

            try {
                val contentToSummarize = _uiState.value.content
                val summary = summarizeNoteUseCase(contentToSummarize)

                summarizeProgressJob?.cancel()
                _uiState.update { it.copy(processingPercent = 100) }
                delay(220)

                _uiState.update {
                    it.copy(
                        isSummarizing = false,
                        activeProcess = null,
                        content = it.content + "\n\n[Tóm tắt]:\n" + summary
                    )
                }
            } catch (e: Exception) {
                summarizeProgressJob?.cancel()
                _uiState.update {
                    it.copy(
                        isSummarizing = false,
                        activeProcess = null,
                        error = "Lỗi tóm tắt: ${e.message ?: "Không xác định"}"
                    )
                }
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
                activeProcess = ActiveProcess.MINDMAP,
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
                        activeProcess = null,
                        mindMap = rootNode,
                        showMindMapDialog = true,
                    )
                }
            } catch (t: Throwable) {
                progressJob.cancel()
                _uiState.update {
                    it.copy(
                        isGeneratingMindMap = false,
                        activeProcess = null,
                        error = "Lỗi tạo MindMap: ${t.message}"
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
