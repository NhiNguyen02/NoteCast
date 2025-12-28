package com.example.notecast.presentation.viewmodel

import androidx.lifecycle.ViewModel
import com.example.notecast.domain.usecase.audio.TranscribeRecordingUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class RecordingViewModel @Inject constructor(
    private val transcribeRecordingUseCase: TranscribeRecordingUseCase,
) : ViewModel() {

    private val _isTranscribing = MutableStateFlow(false)
    val isTranscribing: StateFlow<Boolean> = _isTranscribing.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    suspend fun transcribeRecording(localPath: String, userId: String? = null): String {
        _isTranscribing.value = true
        _error.value = null
        return try {
            transcribeRecordingUseCase(localPath, userId).also {
                _isTranscribing.value = false
            }
        } catch (t: Throwable) {
            _isTranscribing.value = false
            _error.value = t.message
            throw t
        }
    }
}
