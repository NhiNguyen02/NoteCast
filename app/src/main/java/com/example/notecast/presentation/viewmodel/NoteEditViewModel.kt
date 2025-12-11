package com.example.notecast.presentation.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.notecast.domain.model.MindMapNode
import com.example.notecast.domain.model.Note
import com.example.notecast.domain.usecase.GenerateMindMapUseCase
import com.example.notecast.domain.usecase.GetAllFoldersUseCase
import com.example.notecast.domain.usecase.GetNoteByIdUseCase
import com.example.notecast.domain.usecase.NormalizationResult
import com.example.notecast.domain.usecase.NormalizeNoteUseCase
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
import kotlinx.serialization.json.Json
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class NoteEditViewModel @Inject constructor(
    private val getNoteByIdUseCase: GetNoteByIdUseCase,
    private val saveNoteUseCase: SaveNoteUseCase,
    private val getAllFoldersUseCase: GetAllFoldersUseCase,
    private val generateMindMapUseCase: GenerateMindMapUseCase,
    private val normalizeNoteUseCase: NormalizeNoteUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

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
                val savedMindMap = if (!note.mindMapJson.isNullOrBlank()) {
                    try {
                        // Parse chuỗi JSON trong DB thành Object cây để vẽ
                        jsonParser.decodeFromString<MindMapNode>(note.mindMapJson)
                    } catch (e: Exception) { null }
                } else null
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
                        mindMapData = savedMindMap
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
                if (_state.value.title != event.title) {
                    _state.update {
                        it.copy(
                            title = event.title,
                            mindMapData = null
                        )
                    }
                }
            }
            is NoteEditEvent.OnContentChanged -> {
                if (_state.value.content != event.content) {
                    _state.update {
                        it.copy(
                            content = event.content,
                            mindMapData = null
                        )
                    }
                }
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

            //Chuẩn hóa văn bản
            is NoteEditEvent.OnNormalize -> {
                val originalContent = _state.value.content
                if (originalContent.isBlank()) return

                viewModelScope.launch {
                    _state.update { it.copy(isNormalizing = true) }

                    normalizeNoteUseCase(originalContent).collect { result ->
                        when (result) {
                            // 1. Giai đoạn Preview (Heuristic)
                            is NormalizationResult.Preview -> {
                                _state.update {
                                    it.copy(
                                        content = result.text,
                                        isNormalizing = true // Vẫn loading chờ AI
                                    )
                                }
                            }
                            // 2. Giai đoạn Success (AI trả về Object Data)
                            is NormalizationResult.Success -> {
                                val aiData = result.data
                                _state.update {
                                    it.copy(
                                        content = aiData.normalizedText, // Hiển thị text chuẩn
                                        processedTextData = aiData,      // Lưu object để lấy keywords khi Save
                                        isNormalizing = false,
                                        mindMapData = null
                                    )
                                }
                            }
                            // 3. Giai đoạn Lỗi
                            is NormalizationResult.Error -> {
                                _state.update {
                                    it.copy(
                                        isNormalizing = false,
                                        error = "Lỗi chuẩn hóa, dùng bản nháp.",
                                        content = result.text // Dùng bản Preview
                                    )
                                }
                            }
                        }
                    }
                }
            }
            // --- TẠO VÀ LƯU MINDMAP ---
            is NoteEditEvent.OnGenerateMindMap -> {
                // Nếu đã có Mindmap trong state rồi thì hiện luôn, không gọi AI nữa (Tiết kiệm)
                // Nếu muốn ép tạo lại (Regenerate), bạn có thể bỏ check null này
                if (_state.value.mindMapData != null) {
                    _state.update { it.copy(showMindMapDialog = true) }
                    return
                }

                // Bắt đầu hiển thị Dialog Processing
                _state.update { it.copy(isGeneratingMindMap = true, processingPercent = 0) }

                viewModelScope.launch {
                    // 1. Chạy Coroutine tăng phần trăm ảo (0 -> 85%)
                    // Job này sẽ chạy song song với việc gọi AI
                    val progressJob = launch {
                        var p = 0
                        while (p < 85) {
                            delay(200) // Cứ 200ms tăng 1 chút
                            p += (2..5).random()
                            _state.update { it.copy(processingPercent = p) }
                        }
                    }

                    try {
                        val currentId = _state.value.noteId ?: UUID.randomUUID().toString()
                        _state.update { it.copy(noteId = currentId) }
                        val currentNote = Note(
                            id = currentId,
                            title = _state.value.title,
                            content = _state.value.content,
                            noteType = _state.value.noteType,
                            updatedAt = 0, createdAt = 0
                        )

                        // 2. Gọi AI (Tác vụ nặng)
                        val rootNode = generateMindMapUseCase(currentNote)
                        // Khi AI xong, hủy job chạy phần trăm ảo
                        progressJob.cancel()

                        // Nhảy vọt lên 90%
                        _state.update { it.copy(processingPercent = 90) }

                        // 3. Lưu vào DB
                        val jsonString = jsonParser.encodeToString(rootNode)
                        val updatedNote = currentNote.copy(
                            mindMapJson = jsonString,
                            folderId = _state.value.folderId,
                            isFavorite = _state.value.isFavorite,
                            createdAt = _state.value.createdAt,
                            updatedAt = System.currentTimeMillis()
                        )
                        saveNoteUseCase(updatedNote)

                        // 4. Hoàn tất (100%) -> Tắt Dialog -> Hiện Mindmap
                        _state.update { it.copy(processingPercent = 100) }
                        delay(300) // Dừng một chút cho người dùng thấy 100%

                        _state.update {
                            it.copy(
                                isGeneratingMindMap = false,
                                mindMapData = rootNode,
                                showMindMapDialog = true
                            )
                        }
                    } catch (e: Exception) {
                        progressJob.cancel()
                        _state.update {
                            it.copy(
                                isGeneratingMindMap = false,
                                error = "Lỗi tạo Mindmap: ${e.message}"
                            )
                        }
                    }
                }

            }

            is NoteEditEvent.OnCloseMindMap -> {
                _state.update { it.copy(showMindMapDialog = false) }
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
            val idToSave = currentState.noteId ?: UUID.randomUUID().toString()
            val punctuatedTextToSave = currentState.processedTextData?.normalizedText
            val mindMapJsonStr = if (currentState.mindMapData != null) {
                jsonParser.encodeToString(currentState.mindMapData)
            } else null
            saveNoteUseCase(
                Note(
                    id = idToSave,
                    title = currentState.title,
                    content = currentState.content,
                    noteType = currentState.noteType,
                    updatedAt = 0,
                    createdAt = currentState.createdAt,
                    pinTimestamp = currentState.pinTimestamp,
                    tags = emptyList(),
                    isFavorite = currentState.isFavorite, // Lưu trạng thái yêu thích
                    folderId = currentState.folderId,
                    punctuatedText = punctuatedTextToSave,
                    mindMapJson = mindMapJsonStr

                )
            )
            _state.update { it.copy(noteId = idToSave, isSaved = true) }
        }
    }
}