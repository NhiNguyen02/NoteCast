package com.example.notecast.presentation.viewmodel

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.notecast.domain.repository.AudioRepository
import com.example.notecast.domain.usecase.*
import com.example.notecast.presentation.ui.record.RecordingState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

@HiltViewModel
class AudioViewModel @Inject constructor(
    private val audioRepository: AudioRepository,
    private val startRecordingUseCase: StartRecordingUseCase,
    private val stopRecordingUseCase: StopRecordingUseCase,
    private val pauseRecordingUseCase: PauseRecordingUseCase,
    private val resumeRecordingUseCase: ResumeRecordingUseCase,
    private val getRecordingStateUseCase: GetRecordingStateUseCase,
    private val trimAndExportWavUseCase: TrimAndExportWavUseCase,
    private val application: Application // to get external files dir
) : ViewModel() {

    val recordingState: StateFlow<RecordingState> = getRecordingStateUseCase().stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = RecordingState.Idle
    )

    val amplitude = audioRepository.amplitude // or inject repository amplitude directly
    val vadState = audioRepository.vadState
    val waveform = audioRepository.waveform
    val bufferAvailableSamples = audioRepository.bufferAvailableSamples

    // UI processing state
    private val _processing = MutableStateFlow(false)
    val processing: StateFlow<Boolean> = _processing

    private val _processingPercent = MutableStateFlow(0)
    val processingPercent: StateFlow<Int> = _processingPercent

    fun startRecording() {
        startRecordingUseCase()
    }

    fun pauseRecording() {
        pauseRecordingUseCase()
    }

    fun resumeRecording() {
        resumeRecordingUseCase()
    }

    fun stopRecording() {
        stopRecordingUseCase()
    }

    fun processAndSave(prePadding: Int = 1, postPadding: Int = 1, setPending: Boolean = false, onResult: (ProcessResult) -> Unit = {}) {
        viewModelScope.launch {
            _processing.value = true
            _processingPercent.value = 5

            val outDir = application.getExternalFilesDir(null) ?: File(
                application.filesDir,
                "records"
            )
            _processingPercent.value = 20

            val result = trimAndExportWavUseCase(prePadding, postPadding, outDir)
            _processingPercent.value = 100
            delay(150)
            _processing.value = false

            onResult(result)
        }
    }

    // helper getters using repository or injected flows - implement according to your DI
    // e.g. fun getAmplitudeFlow() = audioRepository.amplitude
}

