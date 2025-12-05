package com.example.notecast.presentation.viewmodel

import com.example.notecast.domain.model.AsrChunk
import com.example.notecast.domain.model.ChunkResult
import com.example.notecast.domain.usecase.MergeChunksUseCase
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.notecast.domain.usecase.TranscribeChunkUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class ASRState {
    object Idle : ASRState()
    object Processing : ASRState()
    data class Partial(val text: String) : ASRState()
    data class Final(val text: String) : ASRState()
    data class Error(val msg: String) : ASRState()
}

@HiltViewModel
class ASRViewModel @Inject constructor(
    private val transcribeChunkUseCase: TranscribeChunkUseCase,
    private val mergeChunksUseCase: MergeChunksUseCase,
) : ViewModel() {

    private val _state = MutableStateFlow<ASRState>(ASRState.Idle)
    val state: StateFlow<ASRState> = _state.asStateFlow()

    private val _realtimeTranscript = MutableStateFlow("")
    val realtimeTranscript: StateFlow<String> = _realtimeTranscript.asStateFlow()

    /**
     * Flow nhận SegmentEvent.End(chunk) từ AudioViewModel hoặc domain.
     */
    private val _segments = MutableSharedFlow<AsrChunk>(extraBufferCapacity = 8)
    val segments: SharedFlow<AsrChunk> = _segments.asSharedFlow()

    private val collectedChunks = mutableListOf<ChunkResult>()

    init {
        // Lắng nghe các chunk mới và chạy ASR tuần tự
        viewModelScope.launch {
            segments.collect { chunk ->
                processChunk(chunk)
            }
        }
    }

    /**
     * API để AudioViewModel đẩy chunk sang ASRViewModel.
     */
    fun submitSegment(chunk: AsrChunk) {
        _segments.tryEmit(chunk)
    }

    private fun processChunk(chunk: AsrChunk) {
        viewModelScope.launch {
            try {
                _state.value = ASRState.Processing
                val text = transcribeChunkUseCase(chunk.samples)
                if (text.isNotBlank()) {
                    val result = ChunkResult(
                        startSec = chunk.startSec,
                        endSec = chunk.endSec,
                        text = text,
                    )
                    collectedChunks += result

                    val newText = (_realtimeTranscript.value + " " + text).trim()
                    _realtimeTranscript.value = newText
                    _state.value = ASRState.Partial(newText)
                } else {
                    _state.value = ASRState.Idle
                }
            } catch (t: Throwable) {
                _state.value = ASRState.Error(t.message ?: "ASR error")
            }
        }
    }

    fun finishSession() {
        viewModelScope.launch {
            val finalText = mergeChunksUseCase(collectedChunks)
            _realtimeTranscript.value = finalText
            _state.value = ASRState.Final(finalText)
        }
    }
}
