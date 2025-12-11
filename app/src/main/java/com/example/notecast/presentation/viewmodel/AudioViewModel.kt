package com.example.notecast.presentation.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.notecast.domain.usecase.audio.PauseRecordingUseCase
import com.example.notecast.domain.usecase.audio.ResumeRecordingUseCase
import com.example.notecast.domain.usecase.audio.StartRecordingUseCase
import com.example.notecast.domain.usecase.audio.StopRecordingUseCase
import com.example.notecast.domain.usecase.audio.StreamAudioUseCase
import com.example.notecast.domain.usecase.audio.VadSegmenterUseCase
import com.example.notecast.domain.vad.SegmentEvent
import com.example.notecast.domain.vad.VadState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

enum class RecorderState {
    Idle,
    Recording,
    Paused,
}

private const val TAG_AUDIO = "AudioViewModel"

@HiltViewModel
class AudioViewModel @Inject constructor(
    private val startRecordingUseCase: StartRecordingUseCase,
    private val stopRecordingUseCase: StopRecordingUseCase,
    private val pauseRecordingUseCase: PauseRecordingUseCase,
    private val resumeRecordingUseCase: ResumeRecordingUseCase,
    private val streamAudioUseCase: StreamAudioUseCase,
    private val vadSegmenterUseCase: VadSegmenterUseCase,
    private val audioRepository: com.example.notecast.domain.repository.AudioRepository,
) : ViewModel() {

    private val _recorderState = MutableStateFlow(RecorderState.Idle)
    val recorderState: StateFlow<RecorderState> = _recorderState.asStateFlow()

    private val _amplitude = MutableStateFlow(0f)
    val amplitude: StateFlow<Float> = _amplitude.asStateFlow()

    private val _vadState = MutableStateFlow<VadState>(VadState.SILENT)
    val vadState: StateFlow<VadState> = _vadState.asStateFlow()

    private val _recordingDurationMillis = MutableStateFlow(0L)
    val recordingDurationMillis: StateFlow<Long> = _recordingDurationMillis.asStateFlow()

    private val _waveform = MutableStateFlow<List<Float>>(emptyList())
    val waveform: StateFlow<List<Float>> = _waveform.asStateFlow()

    // Flow phát SegmentEvent ra ngoài nếu UI/logic khác muốn dùng VAD (hiện tại chỉ để monitoring)
    private val _segmentEvents = MutableStateFlow<SegmentEvent>(SegmentEvent.None)
    val segmentEvents: StateFlow<SegmentEvent> = _segmentEvents.asStateFlow()

    private var audioStreamJob: Job? = null
    private var vadJob: Job? = null
    private var timerJob: Job? = null

    fun startRecording() {
        Log.d(TAG_AUDIO, "startRecording: called, currentState=${_recorderState.value}")
        startRecordingUseCase()
        _recorderState.value = RecorderState.Recording
        _recordingDurationMillis.value = 0L
        Log.d(TAG_AUDIO, "startRecording: state=${_recorderState.value}, duration=${_recordingDurationMillis.value}")
        startAudioStreamCollection()
        startVadSegmenterCollection()
        startTimer()
    }

    fun pauseRecording() {
        Log.d(TAG_AUDIO, "pauseRecording: called, currentState=${_recorderState.value}")
        pauseRecordingUseCase()
        _recorderState.value = RecorderState.Paused
        // Cancel các job collection nhưng không clear state
        audioStreamJob?.cancel()
        audioStreamJob = null
        vadJob?.cancel()
        vadJob = null
        timerJob?.cancel()
        timerJob = null
        Log.d(TAG_AUDIO, "pauseRecording: state=${_recorderState.value}, duration=${_recordingDurationMillis.value}")
    }

    fun resumeRecording() {
        Log.d(TAG_AUDIO, "resumeRecording: called, currentState=${_recorderState.value}")
        resumeRecordingUseCase()
        _recorderState.value = RecorderState.Recording
        // Restart tất cả các collection job để tiếp tục nhận data
        startAudioStreamCollection()
        startVadSegmenterCollection()
        startTimer()
        Log.d(TAG_AUDIO, "resumeRecording: state=${_recorderState.value}, duration=${_recordingDurationMillis.value}")
    }

    suspend fun stopRecording() {
        Log.d(TAG_AUDIO, "stopRecording: called, currentState=${_recorderState.value}")
        // Ensure we run stopRecordingUseCase on IO if it's heavy
        try {
            withContext(Dispatchers.IO) {
                stopRecordingUseCase()
            }
            Log.d(
                TAG_AUDIO,
                "stopRecording: stopRecordingUseCase finished, repoFilePath=${audioRepository.currentRecordingFilePath}"
            )
        } catch (t: Throwable) {
            Log.e(TAG_AUDIO, "stopRecording: stopRecordingUseCase error=${t.message}", t)
        }
        _recorderState.value = RecorderState.Idle
        audioStreamJob?.cancel(); audioStreamJob = null
        vadJob?.cancel(); vadJob = null
        timerJob?.cancel(); timerJob = null
        _recordingDurationMillis.value = 0L
        Log.d(
            TAG_AUDIO,
            "stopRecording: state=${_recorderState.value}, duration=${_recordingDurationMillis.value}, repoFilePath=${audioRepository.currentRecordingFilePath}"
        )
        // Kết thúc phiên ghi âm. Luồng ASR hiện được thực thi ở RecordingScreen thông qua TranscribeRecordingUseCase.
    }

    private fun startAudioStreamCollection() {
        if (audioStreamJob?.isActive == true) return
        audioStreamJob = viewModelScope.launch {
            streamAudioUseCase().collect { frame ->
                var max = 0f
                for (v in frame) {
                    val a = kotlin.math.abs(v)
                    if (a > max) max = a
                }
                _amplitude.value = max

                _waveform.update { old ->
                    val newValue = max.coerceIn(0f, 1f)
                    val list = if (old.size >= 64) old.drop(old.size - 63) else old
                    list + newValue
                }
            }
        }
    }

    private fun startVadSegmenterCollection() {
        if (vadJob?.isActive == true) return
        vadJob = viewModelScope.launch {
            vadSegmenterUseCase().collect { event ->
                _segmentEvents.value = event
                when (event) {
                    is SegmentEvent.Start,
                    is SegmentEvent.Continue -> {
                        _vadState.value = VadState.SPEAKING
                    }
                    is SegmentEvent.End -> {
                        _vadState.value = VadState.SILENT
                        // Trước đây tại đây đẩy chunk sang ASRViewModel để xử lý ONNX local.
                        // Sau khi chuyển pipeline sang PhoWhisper remote ASR, ta chỉ dùng VAD cho hiển thị/UX.
                    }
                    SegmentEvent.None -> Unit
                }
            }
        }
    }

    private fun startTimer() {
        if (timerJob?.isActive == true) return
        timerJob = viewModelScope.launch {
            while (true) {
                delay(1000L)
                _recordingDurationMillis.value = _recordingDurationMillis.value + 1000L
            }
        }
    }

    // Expose audio params to UI
    val sampleRate: Int get() = audioRepository.sampleRate
    val channels: Int get() = audioRepository.channels

    // Đường dẫn file WAV 16kHz mono PCM16 của phiên ghi âm hiện tại (sẵn sàng sau khi stopRecordingUseCase hoàn tất)
    val currentRecordingFilePath: String? get() {
        val path = audioRepository.currentRecordingFilePath
        Log.d(TAG_AUDIO, "get currentRecordingFilePath: $path")
        return path
    }
}