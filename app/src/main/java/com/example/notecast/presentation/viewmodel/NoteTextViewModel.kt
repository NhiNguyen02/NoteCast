package com.example.notecast.presentation.viewmodel

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.notecast.data.remote.mapping.parseMindmapRoot
import com.example.notecast.domain.model.MindMapNode
import com.example.notecast.domain.model.NoteDomain
import com.example.notecast.domain.model.NoteStatus
import com.example.notecast.domain.model.NoteType
import com.example.notecast.domain.usecase.notefolder.GetAllFoldersUseCase
import com.example.notecast.domain.usecase.notefolder.GetNoteByIdUseCase
import com.example.notecast.domain.usecase.notefolder.SaveNoteUseCase
import com.example.notecast.domain.usecase.notefolder.CreateNoteOnBackendUseCase
import com.example.notecast.domain.usecase.notefolder.ObserveRemoteNoteUseCase
import com.example.notecast.domain.usecase.postprocess.RegenerateNoteUseCase
import com.example.notecast.domain.usecase.postprocess.RegenerateResult
import com.example.notecast.presentation.ui.notetext.NoteEditEvent
import com.example.notecast.presentation.ui.notetext.NoteEditState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class NoteTextViewModel @Inject constructor(
    private val getNoteByIdUseCase: GetNoteByIdUseCase,
    private val saveNoteUseCase: SaveNoteUseCase,
    private val getAllFoldersUseCase: GetAllFoldersUseCase,
    private val regenerateNoteUseCase: RegenerateNoteUseCase,
    private val createNoteOnBackendUseCase: CreateNoteOnBackendUseCase,
    private val observeRemoteNoteUseCase: ObserveRemoteNoteUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val TAG = "NoteEditVM"

    private val _state = MutableStateFlow(NoteEditState())
    val state = _state.asStateFlow()

    private val noteIdArg: String? = savedStateHandle["noteId"]
    private val jsonParser = Json { ignoreUnknownKeys = true; encodeDefaults = true }

    init {
        loadFolders()
        if (noteIdArg != null && noteIdArg != "new" && noteIdArg != "0") {
            _state.update { it.copy(isLoading = true) }
            loadNote(noteIdArg)
        }
    }

    private fun loadFolders() {
        getAllFoldersUseCase().onEach { folders ->
            _state.update { it.copy(availableFolders = folders) }
            val currentFolder = folders.find { f -> f.id == _state.value.folderId }
            if (currentFolder != null) {
                _state.update { it.copy(folderName = currentFolder.name) }
            }
        }.launchIn(viewModelScope)
    }

    private fun loadNote(id: String) {
        getNoteByIdUseCase(id).onEach { noteDomain ->
            if (noteDomain != null) {
                val folderName = _state.value.availableFolders.find { it.id == noteDomain.folderId }?.name ?: "Chưa phân loại"
                val savedMindMap = noteDomain.mindmapJson
                    ?.takeIf { it.isNotBlank() }
                    ?.let {
                        runCatching { jsonParser.decodeFromString(MindMapNode.serializer(), it) }.getOrNull()
                    }
                _state.update {
                    it.copy(
                        isLoading = false,
                        noteId = noteDomain.id,
                        title = noteDomain.title.orEmpty(),
                        content = noteDomain.rawText.orEmpty(),
                        updatedAt = noteDomain.updatedAt,
                        createdAt = noteDomain.createdAt,
                        folderId = noteDomain.folderId,
                        folderName = folderName,
                        mindMapData = savedMindMap,
                        summary = noteDomain.summary,
                        keywords = noteDomain.keywords,
                    )
                }
            } else {
                _state.update { it.copy(isLoading = false, error = "Không tìm thấy") }
            }
        }.launchIn(viewModelScope)
    }

    /**
     * Đảm bảo note đã tồn tại trên backend trước khi gọi regenerate.
     * Nếu note chưa có noteId hoặc chưa sync, tạo mới trên backend.
     */
    private suspend fun ensureNoteOnBackend(): String? {
        val current = _state.value
        val noteId = current.noteId

        Log.d(TAG, "ensureNoteOnBackend: current noteId=$noteId")

        // Nếu chưa có noteId, tạo note mới trên backend
        if (noteId == null || noteId == "new" || noteId == "0") {
            try {
                val now = System.currentTimeMillis()
                val newId = UUID.randomUUID().toString()

                val note = NoteDomain(
                    id = newId,
                    type = NoteType.TEXT,
                    title = current.title.ifBlank { null },
                    rawText = current.content,
                    normalizedText = null,
                    keywords = emptyList(),
                    summary = null,
                    mindmapJson = null,
                    audio = null,
                    folderId = current.folderId,
                    status = NoteStatus.CREATED,
                    createdAt = now,
                    updatedAt = now,
                )

                Log.d(TAG, "Creating note on backend: id=$newId, title=${note.title}, content length=${current.content.length}")

                // Tạo note trên backend (không generate gì cả, chỉ tạo)
                createNoteOnBackendUseCase(note, generateTasks = emptyList())

                Log.d(TAG, "Successfully created note on backend: $newId")

                // Delay nhỏ để đảm bảo backend đã commit note vào DB
                delay(500)

                // Lưu local
                saveNoteUseCase(note)

                // Cập nhật state
                _state.update { it.copy(noteId = newId, createdAt = now, updatedAt = now) }

                return newId
            } catch (e: Exception) {
                Log.e(TAG, "Failed to create note on backend: ${e.message}", e)
                _state.update { it.copy(error = "Không thể tạo note trên server: ${e.message}") }
                return null
            }
        }

        // Note đã có ID, nhưng có thể chưa được tạo trên backend
        // Thử tạo note trên backend để đảm bảo nó tồn tại
        try {
            val now = System.currentTimeMillis()
            val note = NoteDomain(
                id = noteId,
                type = NoteType.TEXT,
                title = current.title.ifBlank { null },
                rawText = current.content,
                normalizedText = null,
                keywords = current.keywords,
                summary = current.summary,
                mindmapJson = current.mindMapData?.let { jsonParser.encodeToString(MindMapNode.serializer(), it) },
                audio = null,
                folderId = current.folderId,
                status = NoteStatus.CREATED,
                createdAt = current.createdAt.takeIf { it != 0L } ?: now,
                updatedAt = now,
            )

            Log.d(TAG, "Ensuring note exists on backend: id=$noteId")

            // Tạo note trên backend (idempotent - nếu đã tồn tại thì backend sẽ update)
            createNoteOnBackendUseCase(note, generateTasks = emptyList())

            Log.d(TAG, "Note confirmed on backend: $noteId")

            // Delay nhỏ để đảm bảo backend đã commit note vào DB
            delay(500)

            return noteId
        } catch (e: Exception) {
            Log.e(TAG, "Failed to ensure note on backend: ${e.message}", e)
            // Vẫn trả về noteId để thử regenerate, có thể note đã tồn tại
            return noteId
        }
    }

    fun onEvent(event: NoteEditEvent) {
        when (event) {
            is NoteEditEvent.OnTitleChanged -> {
                if (_state.value.title != event.title) {
                    _state.update { it.copy(title = event.title, mindMapData = null) }
                }
            }
            is NoteEditEvent.OnContentChanged -> {
                if (_state.value.content != event.content) {
                    _state.update { it.copy(content = event.content, mindMapData = null) }
                }
            }
            is NoteEditEvent.OnToggleFavorite -> {
                // favorite không còn trong NoteDomain core; nếu cần thì lưu ở nơi khác
            }
            is NoteEditEvent.OnSaveNote -> {
                saveNote(explicit = true)
            }
            is NoteEditEvent.OnFolderSelected -> {
                val folderId = event.folder?.id
                val folderName = event.folder?.name ?: "Chưa phân loại"
                _state.update { it.copy(folderId = folderId, folderName = folderName) }

                // Auto-save khi chọn folder và tạo note trên backend nếu chưa có
                viewModelScope.launch {
                    try {
                        val noteId = ensureNoteOnBackend()
                        if (noteId != null) {
                            Log.d(TAG, "Note auto-saved after folder selection: $noteId → folder: $folderName")
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Error auto-saving note after folder selection: ${e.message}", e)
                    }
                }
            }
            is NoteEditEvent.OnNormalize -> {
                viewModelScope.launch {
                    _state.update { it.copy(isNormalizing = true, error = null) }

                    val current = _state.value
                    val noteId = current.noteId

                    // Nếu chưa có noteId, tạo note mới VÀ generate ngay
                    if (noteId == null || noteId == "new" || noteId == "0") {
                        try {
                            val now = System.currentTimeMillis()
                            val newId = UUID.randomUUID().toString()

                            val note = NoteDomain(
                                id = newId,
                                type = NoteType.TEXT,
                                title = current.title.ifBlank { null },
                                rawText = current.content,
                                normalizedText = null,
                                keywords = emptyList(),
                                summary = null,
                                mindmapJson = null,
                                audio = null,
                                folderId = current.folderId,
                                status = NoteStatus.CREATED,
                                createdAt = now,
                                updatedAt = now,
                            )

                            Log.d(TAG, "Creating note on backend with generate: id=$newId, tasks=[normalize, keywords]")

                            // Tạo note trên backend VÀ generate normalize + keywords ngay
                            // Backend sẽ tự động enqueue enrichment job
                            createNoteOnBackendUseCase(note, generateTasks = listOf("normalize", "keywords"))

                            // Lưu local
                            saveNoteUseCase(note)

                            // Cập nhật state với noteId mới
                            _state.update { it.copy(noteId = newId, createdAt = now, updatedAt = now) }

                            Log.d(TAG, "Note created, observing SSE for updates: $newId")

                            // Observe SSE để nhận kết quả enrichment từ backend
                            // KHÔNG gọi regenerate vì backend đã enqueue job rồi
                            observeRemoteNoteUseCase(newId).collect { updatedNote ->
                                Log.d(TAG, "Received note update via SSE: status=${updatedNote.status}, hasNormalized=${updatedNote.normalizedText != null}")

                                _state.update {
                                    it.copy(
                                        isNormalizing = updatedNote.status == NoteStatus.PROCESSING,
                                        content = updatedNote.normalizedText ?: updatedNote.rawText.orEmpty(),
                                        keywords = updatedNote.keywords,
                                    )
                                }
                                saveNote(explicit = false, fromDomain = updatedNote)

                                // Stop observing khi done
                                if (updatedNote.status == NoteStatus.READY) {
                                    Log.d(TAG, "Normalization complete, stopping SSE observation")
                                    return@collect
                                }
                            }
                        } catch (e: Exception) {
                            Log.e(TAG, "Failed to create and normalize note: ${e.message}", e)
                            _state.update { it.copy(isNormalizing = false, error = "Không thể tạo note: ${e.message}") }
                        }
                    } else {
                        // Note đã có ID - ensure tồn tại trên backend trước khi regenerate
                        try {
                            Log.d(TAG, "Note exists locally: $noteId, ensuring on backend...")
                            val confirmedId = ensureNoteOnBackend()

                            if (confirmedId == null) {
                                _state.update { it.copy(isNormalizing = false, error = "Không thể tạo note trên server") }
                                return@launch
                            }

                            Log.d(TAG, "Note confirmed on backend, calling regenerate: $confirmedId")

                            regenerateNoteUseCase(
                                noteId = confirmedId,
                                generate = listOf("normalize", "keywords"),
                            ).collect { result ->
                                when (result) {
                                    is RegenerateResult.Loading -> {
                                        _state.update { it.copy(isNormalizing = true) }
                                    }
                                    is RegenerateResult.Success -> {
                                        val updatedNote = result.note
                                        _state.update {
                                            it.copy(
                                                isNormalizing = false,
                                                content = updatedNote.normalizedText ?: updatedNote.rawText.orEmpty(),
                                                keywords = updatedNote.keywords,
                                            )
                                        }
                                        saveNote(explicit = false, fromDomain = updatedNote)
                                    }
                                    is RegenerateResult.Error -> {
                                        _state.update { it.copy(isNormalizing = false, error = result.message) }
                                    }
                                }
                            }
                        } catch (e: Exception) {
                            Log.e(TAG, "Failed to ensure note on backend: ${e.message}", e)
                            _state.update { it.copy(isNormalizing = false, error = "Lỗi: ${e.message}") }
                        }
                    }
                }
            }
            is NoteEditEvent.OnSummarize -> {
                viewModelScope.launch {
                    _state.update { it.copy(isSummarizing = true, error = null) }

                    val current = _state.value
                    val noteId = current.noteId

                    // Nếu chưa có noteId, tạo note mới VÀ generate ngay
                    if (noteId == null || noteId == "new" || noteId == "0") {
                        try {
                            val now = System.currentTimeMillis()
                            val newId = UUID.randomUUID().toString()

                            val note = NoteDomain(
                                id = newId,
                                type = NoteType.TEXT,
                                title = current.title.ifBlank { null },
                                rawText = current.content,
                                normalizedText = null,
                                keywords = emptyList(),
                                summary = null,
                                mindmapJson = null,
                                audio = null,
                                folderId = current.folderId,
                                status = NoteStatus.CREATED,
                                createdAt = now,
                                updatedAt = now,
                            )

                            Log.d(TAG, "Creating note on backend with generate summary: id=$newId")

                            // Tạo note trên backend VÀ generate summary ngay
                            createNoteOnBackendUseCase(note, generateTasks = listOf("summary"))

                            // Lưu local
                            saveNoteUseCase(note)

                            // Cập nhật state
                            _state.update { it.copy(noteId = newId, createdAt = now, updatedAt = now) }

                            // Observe SSE để nhận kết quả
                            observeRemoteNoteUseCase(newId).collect { updatedNote ->
                                _state.update {
                                    it.copy(
                                        isSummarizing = updatedNote.status == NoteStatus.PROCESSING,
                                        summary = updatedNote.summary
                                    )
                                }
                                saveNote(explicit = false, fromDomain = updatedNote)

                                if (updatedNote.status == NoteStatus.READY) {
                                    return@collect
                                }
                            }
                        } catch (e: Exception) {
                            Log.e(TAG, "Failed to create and summarize note: ${e.message}", e)
                            _state.update { it.copy(isSummarizing = false, error = "Không thể tạo note: ${e.message}") }
                        }
                    } else {
                        // Note đã có ID - ensure tồn tại trên backend trước khi regenerate
                        try {
                            val confirmedId = ensureNoteOnBackend()
                            if (confirmedId == null) {
                                _state.update { it.copy(isSummarizing = false, error = "Không thể tạo note trên server") }
                                return@launch
                            }

                            regenerateNoteUseCase(noteId = confirmedId, generate = listOf("summary")).collect { result ->
                                when (result) {
                                    is RegenerateResult.Loading -> _state.update { it.copy(isSummarizing = true) }
                                    is RegenerateResult.Success -> {
                                        val updatedNote = result.note
                                        _state.update { it.copy(isSummarizing = false, summary = updatedNote.summary) }
                                        saveNote(explicit = false, fromDomain = updatedNote)
                                    }
                                    is RegenerateResult.Error -> {
                                        _state.update { it.copy(isSummarizing = false, error = result.message) }
                                    }
                                }
                            }
                        } catch (e: Exception) {
                            Log.e(TAG, "Failed to ensure note on backend: ${e.message}", e)
                            _state.update { it.copy(isSummarizing = false, error = "Lỗi: ${e.message}") }
                        }
                    }
                }
            }
            is NoteEditEvent.OnGenerateMindMap -> {
                if (_state.value.mindMapData != null) {
                    _state.update { it.copy(showMindMapDialog = true) }
                    return
                }

                viewModelScope.launch {
                    _state.update { it.copy(isGeneratingMindMap = true, error = null) }

                    val current = _state.value
                    val noteId = current.noteId

                    // Nếu chưa có noteId, tạo note mới VÀ generate ngay
                    if (noteId == null || noteId == "new" || noteId == "0") {
                        try {
                            val now = System.currentTimeMillis()
                            val newId = UUID.randomUUID().toString()

                            val note = NoteDomain(
                                id = newId,
                                type = NoteType.TEXT,
                                title = current.title.ifBlank { null },
                                rawText = current.content,
                                normalizedText = null,
                                keywords = emptyList(),
                                summary = null,
                                mindmapJson = null,
                                audio = null,
                                folderId = current.folderId,
                                status = NoteStatus.CREATED,
                                createdAt = now,
                                updatedAt = now,
                            )

                            Log.d(TAG, "Creating note on backend with generate mindmap: id=$newId")

                            // Tạo note trên backend VÀ generate mindmap ngay
                            createNoteOnBackendUseCase(note, generateTasks = listOf("mindmap"))

                            // Lưu local
                            saveNoteUseCase(note)

                            // Cập nhật state
                            _state.update { it.copy(noteId = newId, createdAt = now, updatedAt = now) }

                            // Observe SSE để nhận kết quả
                            observeRemoteNoteUseCase(newId).collect { updatedNote ->
                                val mindmap = updatedNote.parseMindmapRoot()
                                _state.update {
                                    it.copy(
                                        isGeneratingMindMap = updatedNote.status == NoteStatus.PROCESSING,
                                        mindMapData = mindmap,
                                    )
                                }
                                saveNote(explicit = false, fromDomain = updatedNote)

                                if (updatedNote.status == NoteStatus.READY) {
                                    return@collect
                                }
                            }
                        } catch (e: Exception) {
                            Log.e(TAG, "Failed to create and generate mindmap: ${e.message}", e)
                            _state.update { it.copy(isGeneratingMindMap = false, error = "Không thể tạo note: ${e.message}") }
                        }
                    } else {
                        // Note đã có ID - ensure tồn tại trên backend trước khi regenerate
                        try {
                            val confirmedId = ensureNoteOnBackend()
                            if (confirmedId == null) {
                                _state.update { it.copy(isGeneratingMindMap = false, error = "Không thể tạo note trên server") }
                                return@launch
                            }

                            regenerateNoteUseCase(noteId = confirmedId, generate = listOf("mindmap")).collect { result ->
                                when (result) {
                                    is RegenerateResult.Loading -> {
                                        _state.update { it.copy(isGeneratingMindMap = true) }
                                    }
                                    is RegenerateResult.Success -> {
                                        val updatedNote = result.note
                                        val mindmap = updatedNote.parseMindmapRoot()
                                        _state.update {
                                            it.copy(
                                                isGeneratingMindMap = false,
                                                mindMapData = mindmap,
                                            )
                                        }
                                        saveNote(explicit = false, fromDomain = updatedNote)
                                    }
                                    is RegenerateResult.Error -> {
                                        _state.update { it.copy(isGeneratingMindMap = false, error = result.message) }
                                    }
                                }
                            }
                        } catch (e: Exception) {
                            Log.e(TAG, "Failed to ensure note on backend: ${e.message}", e)
                            _state.update { it.copy(isGeneratingMindMap = false, error = "Lỗi: ${e.message}") }
                        }
                    }
                }
            }
            NoteEditEvent.OnCloseMindMap -> {
                _state.update { it.copy(showMindMapDialog = false) }
            }
            NoteEditEvent.OnClearSavedDialog -> {
                _state.update { it.copy(showSavedDialog = false) }
            }
        }
    }

    private fun saveNote(explicit: Boolean, fromDomain: NoteDomain? = null) {
        val current = _state.value
        viewModelScope.launch {
            try {
                val now = System.currentTimeMillis()
                val createdAt = current.createdAt.takeIf { it != 0L } ?: now
                val id = current.noteId ?: UUID.randomUUID().toString()

                val baseDomain = fromDomain ?: NoteDomain(
                    id = id,
                    type = NoteType.TEXT,
                    title = current.title.ifBlank { null },
                    rawText = current.content,
                    normalizedText = null,
                    keywords = current.keywords,
                    summary = current.summary,
                    mindmapJson = current.mindMapData?.let { jsonParser.encodeToString(MindMapNode.serializer(), it) },
                    audio = null,
                    folderId = current.folderId,
                    status = NoteStatus.CREATED,
                    createdAt = createdAt,
                    updatedAt = now,
                )

                saveNoteUseCase(baseDomain)
                _state.update {
                    it.copy(
                        noteId = baseDomain.id,
                        updatedAt = now,
                        isSaved = explicit,
                        showSavedDialog = explicit,
                    )
                }
            } catch (t: Throwable) {
                Log.e(TAG, "Error saving note: ${t.message}", t)
                _state.update { it.copy(error = t.message) }
            }
        }
    }
}