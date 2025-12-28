package com.example.notecast.presentation.viewmodel

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.notecast.data.remote.mapping.parseMindmapRoot
import com.example.notecast.domain.model.AudioDomain
import com.example.notecast.domain.model.Folder
import com.example.notecast.domain.model.MindMapNode
import com.example.notecast.domain.model.NoteDomain
import com.example.notecast.domain.model.NoteStatus
import com.example.notecast.domain.model.NoteType
import com.example.notecast.domain.usecase.notefolder.GetAllFoldersUseCase
import com.example.notecast.domain.usecase.notefolder.GetNoteByIdUseCase
import com.example.notecast.domain.usecase.notefolder.SaveNoteUseCase
import com.example.notecast.domain.usecase.postprocess.RegenerateNoteUseCase
import com.example.notecast.domain.usecase.postprocess.RegenerateResult
import com.example.notecast.domain.usecase.notefolder.ObserveRemoteNoteUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class NoteAudioViewModel @Inject constructor(
    private val saveNoteUseCase: SaveNoteUseCase,
    private val getAllFoldersUseCase: GetAllFoldersUseCase,
    private val regenerateNoteUseCase: RegenerateNoteUseCase,
    getNoteByIdUseCase: GetNoteByIdUseCase,
    private val observeRemoteNoteUseCase: ObserveRemoteNoteUseCase,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    enum class ProcessingStep { TRANSCRIBING, NORMALIZING, SUMMARIZING, MINDMAP, DONE }

    data class UiState(
        val noteId: String? = null,          // = backend note_id
        val type: NoteType = NoteType.AUDIO,
        val title: String = "",
        val rawText: String? = null,
        val normalizedText: String? = null,
        val keywords: List<String> = emptyList(),
        val summary: String? = null,
        val mindMap: MindMapNode? = null,
        val audio: AudioDomain? = null,      // duration + chunks
        val status: NoteStatus = NoteStatus.CREATED,

        val createdAt: Long = 0L,
        val updatedAt: Long = 0L,

        // UI-only
        val isProcessing: Boolean = false,
        val currentStep: ProcessingStep = ProcessingStep.TRANSCRIBING,
        val showSavedDialog: Boolean = false,
        val error: String? = null,
        val availableFolders: List<Folder> = emptyList(),
        val folderId: String? = null,
        val folderName: String = "Chưa phân loại",
    )

    private fun UiState.withStepFromStatus(status: NoteStatus, rawStatus: String?): UiState {
        val step = when (rawStatus) {
            "processing"      -> ProcessingStep.TRANSCRIBING
            "normalize_done"  -> ProcessingStep.NORMALIZING
            "summary_done"    -> ProcessingStep.SUMMARIZING
            "mindmap_done"    -> ProcessingStep.MINDMAP
            "ready"           -> ProcessingStep.DONE
            else -> when (status) {
                NoteStatus.CREATED    -> ProcessingStep.TRANSCRIBING
                NoteStatus.PROCESSING -> this.currentStep
                NoteStatus.READY      -> ProcessingStep.DONE
                NoteStatus.ERROR      -> this.currentStep
            }
        }
        return copy(
            currentStep = step,
            isProcessing = (status == NoteStatus.PROCESSING)
        )
    }

    private val TAG_NOTE_DETAIL = "NoteDetailVM"

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    private val json = Json { ignoreUnknownKeys = true; encodeDefaults = true }

    // Job để quản lý SSE subscription, có thể hủy khi không cần nữa
    private var sseSubscriptionJob: Job? = null

    // Job để debounce auto-save khi title thay đổi
    private var autoSaveJob: Job? = null

    private fun UiState.updateFromDomain(note: NoteDomain): UiState {
        val mindmap = note.parseMindmapRoot()
        return copy(
            noteId = note.id,
            type = note.type,
            title = note.title.orEmpty(),
            rawText = note.rawText,
            normalizedText = note.normalizedText,
            keywords = note.keywords,
            summary = note.summary,
            mindMap = mindmap,
            audio = note.audio,
            status = note.status,
            folderId = note.folderId,
            createdAt = note.createdAt,
            updatedAt = note.updatedAt,
        ).withStepFromStatus(note.status, note.statusRaw)
    }

    private fun startNoteEventsSubscription(noteId: String) {
        // Hủy subscription cũ nếu có
        sseSubscriptionJob?.cancel()

        // CHỈ subscribe SSE khi note đang processing
        val currentStatus = _uiState.value.status
        if (currentStatus == NoteStatus.READY) {
            Log.d(TAG_NOTE_DETAIL, "Note $noteId is already READY, skip SSE subscription")
            return
        }

        Log.d(TAG_NOTE_DETAIL, "Starting SSE subscription for note $noteId (status=$currentStatus)")

        sseSubscriptionJob = viewModelScope.launch {
            observeRemoteNoteUseCase(noteId).collectLatest { remoteNote ->
                _uiState.update { it.updateFromDomain(remoteNote) }
                saveNoteUseCase(remoteNote)

                // Tự động HỦY subscription khi note đã xử lý xong
                if (remoteNote.status == NoteStatus.READY) {
                    Log.d(TAG_NOTE_DETAIL, "Note $noteId is now READY, cancelling SSE subscription")
                    sseSubscriptionJob?.cancel()
                }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        // Đảm bảo hủy SSE connection khi ViewModel bị destroy
        sseSubscriptionJob?.cancel()
        // Hủy auto-save job nếu có
        autoSaveJob?.cancel()
        Log.d(TAG_NOTE_DETAIL, "ViewModel cleared, SSE subscription cancelled")
    }

    init {
        val noteIdArg: String? = savedStateHandle["noteId"]
        Log.d(TAG_NOTE_DETAIL, "Init NoteDetailViewModel with noteId='$noteIdArg'")

        if (noteIdArg != null) {
            getNoteByIdUseCase(noteIdArg).onEach { noteDomain ->
                if (noteDomain != null) {
                    _uiState.update { it.updateFromDomain(noteDomain) }
                    // Bắt đầu quan sát remote sau khi có noteId hợp lệ
                    startNoteEventsSubscription(noteDomain.id)
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

        // Cập nhật UI state
        _uiState.update {
            it.copy(
                folderId = folderId,
                folderName = folderName,
            )
        }

        // Tự động lưu note khi chọn folder
        viewModelScope.launch {
            try {
                val current = _uiState.value
                val now = System.currentTimeMillis()
                val createdAt = current.createdAt.takeIf { it != 0L } ?: now
                val noteId = current.noteId ?: UUID.randomUUID().toString()
                val mindMapJson = current.mindMap?.let { json.encodeToString(MindMapNode.serializer(), it) }

                val note = NoteDomain(
                    id = noteId,
                    type = current.type,
                    title = current.title,
                    rawText = current.rawText,
                    normalizedText = current.normalizedText,
                    keywords = current.keywords,
                    summary = current.summary,
                    mindmapJson = mindMapJson,
                    audio = current.audio,
                    folderId = folderId,  // Sử dụng folderId mới được chọn
                    status = current.status,
                    createdAt = createdAt,
                    updatedAt = now,
                )

                saveNoteUseCase(note)
                _uiState.update { it.copy(noteId = noteId, updatedAt = now) }

                Log.d(TAG_NOTE_DETAIL, "Note auto-saved after folder selection: $noteId → folder: $folderName")
            } catch (t: Throwable) {
                Log.e(TAG_NOTE_DETAIL, "Error auto-saving note after folder selection: ${t.message}", t)
            }
        }
    }

    fun onTitleChanged(newTitle: String) {
        _uiState.update { it.copy(title = newTitle, mindMap = null) }

        // Auto-save sau 2 giây khi user ngừng gõ
        autoSaveJob?.cancel()
        autoSaveJob = viewModelScope.launch {
            delay(2000) // Đợi 2 giây
            try {
                val current = _uiState.value
                val noteId = current.noteId

                // Chỉ auto-save nếu đã có noteId (note đã được tạo)
                if (noteId != null) {
                    val now = System.currentTimeMillis()
                    val mindMapJson = current.mindMap?.let { json.encodeToString(MindMapNode.serializer(), it) }

                    val note = NoteDomain(
                        id = noteId,
                        type = current.type,
                        title = newTitle,
                        rawText = current.rawText,
                        normalizedText = current.normalizedText,
                        keywords = current.keywords,
                        summary = current.summary,
                        mindmapJson = mindMapJson,
                        audio = current.audio,
                        folderId = current.folderId,
                        status = current.status,
                        createdAt = current.createdAt,
                        updatedAt = now,
                    )

                    saveNoteUseCase(note)
                    _uiState.update { it.copy(updatedAt = now) }

                    Log.d(TAG_NOTE_DETAIL, "Note auto-saved after title change: $noteId")
                }
            } catch (t: Throwable) {
                Log.e(TAG_NOTE_DETAIL, "Error auto-saving note after title change: ${t.message}", t)
            }
        }
    }

    fun onRegenerateAllClicked() {
        val noteId = _uiState.value.noteId ?: return
        viewModelScope.launch {
            regenerateNoteUseCase(
                noteId = noteId,
                generate = listOf("normalize", "keywords", "summary", "mindmap")
            ).collect { result ->
                when (result) {
                    is RegenerateResult.Loading -> {
                        _uiState.update {
                            it.copy(
                                isProcessing = true,
                                currentStep = ProcessingStep.NORMALIZING,
                                error = null,
                            )
                        }
                    }
                    is RegenerateResult.Success -> {
                        val note = result.note
                        val mindmap = note.parseMindmapRoot()
                        _uiState.update {
                            it.copy(
                                isProcessing = false,
                                currentStep = ProcessingStep.DONE,
                                rawText = note.rawText,
                                normalizedText = note.normalizedText,
                                keywords = note.keywords,
                                summary = note.summary,
                                mindMap = mindmap,
                                audio = note.audio,
                                status = note.status,
                                createdAt = note.createdAt,
                                updatedAt = note.updatedAt,
                            )
                        }
                        saveNoteUseCase(note)
                    }
                    is RegenerateResult.Error -> {
                        _uiState.update {
                            it.copy(
                                isProcessing = false,
                                error = result.message
                            )
                        }
                    }
                }
            }
        }
    }

    fun onSaveNote() {
        val current = _uiState.value
        viewModelScope.launch {
            try {
                val now = System.currentTimeMillis()
                val createdAt = current.createdAt.takeIf { it != 0L } ?: now
                val noteId = current.noteId ?: UUID.randomUUID().toString()
                val mindMapJson = current.mindMap?.let { json.encodeToString(MindMapNode.serializer(), it) }

                val note = NoteDomain(
                    id = noteId,
                    type = current.type,
                    title = current.title,
                    rawText = current.rawText,
                    normalizedText = current.normalizedText,
                    keywords = current.keywords,
                    summary = current.summary,
                    mindmapJson = mindMapJson,
                    audio = current.audio,
                    folderId = current.folderId,
                    status = current.status,
                    createdAt = createdAt,
                    updatedAt = now,
                )
                saveNoteUseCase(note)
                _uiState.update { it.copy(noteId = noteId, showSavedDialog = true, updatedAt = now) }
            } catch (t: Throwable) {
                Log.e(TAG_NOTE_DETAIL, "Error saving note: ${t.message}", t)
                _uiState.update { it.copy(error = t.message) }
            }
        }
    }

    fun clearSavedDialog() {
        _uiState.update { it.copy(showSavedDialog = false) }
    }
}
