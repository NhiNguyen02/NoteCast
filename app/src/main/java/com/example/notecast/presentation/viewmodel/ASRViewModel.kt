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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File
import java.net.SocketTimeoutException
import javax.inject.Inject
import java.util.UUID

private const val TAG_ASR = "ASRViewModel"

sealed class ASRState {
    object Idle : ASRState()
    object Processing : ASRState()
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

    private val _lastSavedNoteId = MutableStateFlow<String?>(null)
    val lastSavedNoteId: StateFlow<String?> = _lastSavedNoteId.asStateFlow()


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
                _state.value = ASRState.Processing
                val result = transcribeRecordingUseCase(file)

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
    }

    fun saveVoiceNote(
        title: String,
        transcript: String,
        chunksJson: String?,
        audioFilePath: String,
        durationMs: Long?,
        folderId: String? = null,
    ) {
        viewModelScope.launch {
            val noteId = UUID.randomUUID().toString()
            val note = Note(
                id = noteId,                 // để SaveNoteUseCase tự sinh UUID
                noteType = "VOICE",
                title = title,
                content = transcript,
                rawText = transcript,
                timestampsJson = chunksJson,
                filePath = audioFilePath,
                durationMs = durationMs,
                createdAt = 0L,         // để UseCase set now
                updatedAt = 0L,
                folderId = folderId,
                // các field khác để default
            )
            saveNoteUseCase(note)
            _lastSavedNoteId.value = noteId
        }
    }
}
