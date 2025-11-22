package com.example.notecast.presentation.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import androidx.lifecycle.viewModelScope
import com.example.notecast.data.local.Transcript
import com.example.notecast.domain.usecase.TranscribeTrimmedRecordingUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class ASRViewModel @Inject constructor(
    private val transcribeTrimmedRecordingUseCase: TranscribeTrimmedRecordingUseCase
) : ViewModel() {

    private val TAG = "ASRViewModel"
    private val _transcript = MutableStateFlow<Transcript?>(null)
    val transcript: StateFlow<Transcript?> = _transcript
    private val _isTranscribing = MutableStateFlow(false)
    val isTranscribing: StateFlow<Boolean> = _isTranscribing
    /**
     * Transcribe PCM được trim sẵn, không cần truyền dữ liệu từ UI
     * prePadding / postPadding tương ứng với logic trim trong AudioRepository
     */
    fun startTranscriptionTrimmed(prePadding: Int = 1, postPadding: Int = 1) {
        viewModelScope.launch {
            _isTranscribing.value = true
            Log.d(TAG, "startTranscriptionTrimmed: started (pre=$prePadding post=$postPadding)")
            try {
                val result = transcribeTrimmedRecordingUseCase(prePadding, postPadding)
                _transcript.value = result
                Log.d(TAG, "startTranscriptionTrimmed: completed, textLength=${result.text.length}")
            } catch (e: Exception) {
                _transcript.value = Transcript(text = "", timestamp = System.currentTimeMillis())
                Log.e(TAG, "startTranscriptionTrimmed: error ${e.message}", e)
            } finally {
                _isTranscribing.value = false
            }
        }
    }
    /**
     * Nếu bạn muốn truyền PCM trực tiếp vẫn giữ method cũ
     */
    fun startTranscription(pcm: ShortArray, sampleRate: Int = 16000) {
        viewModelScope.launch {
            _isTranscribing.value = true
            Log.d(TAG, "startTranscription: started, pcm.length=${pcm.size} sampleRate=$sampleRate")
            try {
                val result = transcribeTrimmedRecordingUseCase.transcribeDirect(pcm, sampleRate)
                _transcript.value = result
                Log.d(TAG, "startTranscription: completed, textLength=${result.text.length}")
            } catch (e: Exception) {
                _transcript.value = Transcript(text = "", timestamp = System.currentTimeMillis())
                Log.e(TAG, "startTranscription: error ${e.message}", e)
            } finally {
                _isTranscribing.value = false
            }
        }
    }
    fun clearTranscript() {
        _transcript.value = null
    }
}
