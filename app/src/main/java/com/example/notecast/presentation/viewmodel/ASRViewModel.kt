package com.example.notecast.presentation.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.notecast.domain.model.AsrResult
import com.example.notecast.domain.model.ChunkResult
import com.example.notecast.domain.model.Note
import com.example.notecast.domain.usecase.asr.TranscribeRecordingUseCase
import com.example.notecast.domain.usecase.notefolder.SaveNoteUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.File
import java.net.SocketTimeoutException
import javax.inject.Inject
import java.util.UUID

private const val TAG_ASR = "ASRViewModel"

sealed class ASRState {
    object Idle : ASRState()
    /**
     * Optional progress percent for long-running operations such as ASR.
     * If null, UI can choose to show indeterminate spinner.
     */
    data class Processing(val percent: Int? = null) : ASRState()
    data class Final(val result: AsrResult) : ASRState()
    data class Error(val msg: String) : ASRState()
}

@HiltViewModel
class ASRViewModel @Inject constructor(
    private val transcribeRecordingUseCase: TranscribeRecordingUseCase,
    private val saveNoteUseCase: SaveNoteUseCase,
) : ViewModel() {

    private val _state = MutableStateFlow<ASRState>(ASRState.Idle)
    val state: StateFlow<ASRState> = _state.asStateFlow()

    private val _transcript = MutableStateFlow("")
    val transcript: StateFlow<String> = _transcript.asStateFlow()

    // Derived progress percent for consumers that only care about progress
    val progressPercent: StateFlow<Int> = _state
        .map { state ->
            when (state) {
                is ASRState.Processing -> state.percent ?: 0
                else -> 0
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0
        )

    /**
     * Remote ASR: upload local audio file to Firebase Storage and call backend PhoWhisper.
     * API đơn giản cho RecordingScreen: chỉ cần truyền đường dẫn file.
     */
    fun transcribeRecordingFile(audioFilePath: String) {
        Log.d(TAG_ASR, "transcribeRecordingFile: called with path=$audioFilePath")
        val file = File(audioFilePath)
        if (!file.exists()) {
            Log.e(TAG_ASR, "transcribeRecordingFile: file does not exist at $audioFilePath")
            _state.value = ASRState.Error("File ghi âm không tồn tại")
            return
        }

        viewModelScope.launch {
            try {
                // start with 0%
                _state.value = ASRState.Processing(percent = 0)
                val result = transcribeRecordingUseCase(file)

                // once done, we can briefly set it to 100% before Final if desired
                _state.value = ASRState.Processing(percent = 100)

                // --- LOG CHI TIẾT DỮ LIỆU TRẢ VỀ TỪ BACKEND ---
                Log.d(
                    TAG_ASR,
                    "ASR backend result: textLength=${result.text.length}, chunksCount=${result.chunks.size}"
                )
                if (result.chunks.isNotEmpty()) {
                    val firstChunk: ChunkResult = result.chunks.first()
                    Log.d(
                        TAG_ASR,
                        "First chunk: startSec=${firstChunk.startSec}, endSec=${firstChunk.endSec}, text='${firstChunk.text.take(80)}'"
                    )
                } else {
                    Log.d(TAG_ASR, "ASR backend returned NO chunks")
                }

                _transcript.value = result.text
                _state.value = ASRState.Final(result)
            } catch (t: Throwable) {
                if (t is CancellationException) throw t
                Log.e(TAG_ASR, "transcribeRecordingFile error: ${t.message}", t)
                val msg = if (t is SocketTimeoutException) {
                    "Hệ thống nhận diện quá lâu, vui lòng thử lại với đoạn ghi âm ngắn hơn."
                } else {
                    "Không thể nhận diện giọng nói, vui lòng thử lại."
                }
                _state.value = ASRState.Error(msg)
            }
        }
    }

    fun resetSession() {
        _transcript.value = ""
        _state.value = ASRState.Idle
        // lastSavedNoteId is removed; session reset only clears transcript & state
    }

    suspend fun saveVoiceNoteAndReturnId(
        title: String,
        transcript: String,
        chunksJson: String?,
        audioFilePath: String,
        durationMs: Long?,
        folderId: String? = null,
    ): String {
        val noteId = UUID.randomUUID().toString()
        val note = Note(
            id = noteId,
            noteType = "VOICE",
            title = title,
            content = transcript,
            rawText = transcript,
            timestampsJson = chunksJson,
            filePath = audioFilePath,
            durationMs = durationMs,
            createdAt = 0L,
            updatedAt = 0L,
            folderId = folderId,
        )
        saveNoteUseCase(note)
        Log.d(TAG_ASR, "Voice note saved, returning noteId=$noteId")
        return noteId
    }
}
