package com.example.notecast.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.notecast.domain.usecase.PauseRecordingUseCase
import com.example.notecast.domain.usecase.ResumeRecordingUseCase
import com.example.notecast.domain.usecase.StartRecordingUseCase
import com.example.notecast.domain.usecase.StopRecordingUseCase
import com.example.notecast.domain.usecase.StreamAudioUseCase
import com.example.notecast.domain.usecase.VadSegmenterUseCase
import com.example.notecast.domain.vad.SegmentEvent
import com.example.notecast.domain.vad.VadState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class RecorderState {
    Idle,
    Recording,
    Paused,
}

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

    // Flow phát SegmentEvent ra ngoài cho ASRViewModel/domain nếu cần
    private val _segmentEvents = MutableStateFlow<SegmentEvent>(SegmentEvent.None)
    val segmentEvents: StateFlow<SegmentEvent> = _segmentEvents.asStateFlow()

    private var audioStreamJob: Job? = null
    private var vadJob: Job? = null
    private var timerJob: Job? = null

    /**
     * Tham chiếu tới ASRViewModel, được set từ UI (Compose) khi cần phối hợp.
     * Tránh inject vòng tròn giữa hai ViewModel.
     */
    private var asrViewModel: ASRViewModel? = null

    fun attachAsrViewModel(vm: ASRViewModel) {
        asrViewModel = vm
    }

    fun startRecording() {
        startRecordingUseCase()
        _recorderState.value = RecorderState.Recording
        _recordingDurationMillis.value = 0L
        asrViewModel?.resetSession()
        startAudioStreamCollection()
        startVadSegmenterCollection()
        startTimer()
    }

    fun pauseRecording() {
        pauseRecordingUseCase()
        _recorderState.value = RecorderState.Paused
        // Cancel các job collection nhưng không clear state
        audioStreamJob?.cancel()
        audioStreamJob = null
        vadJob?.cancel()
        vadJob = null
        timerJob?.cancel()
        timerJob = null
    }

    fun resumeRecording() {
        resumeRecordingUseCase()
        _recorderState.value = RecorderState.Recording
        // Restart tất cả các collection job để tiếp tục nhận data
        startAudioStreamCollection()
        startVadSegmenterCollection()
        startTimer()
    }

    fun stopRecording() {
        viewModelScope.launch {
            stopRecordingUseCase()
            _recorderState.value = RecorderState.Idle
            audioStreamJob?.cancel(); audioStreamJob = null
            vadJob?.cancel(); vadJob = null
            timerJob?.cancel(); timerJob = null
            _recordingDurationMillis.value = 0L
            // Việc kết thúc phiên ASR và lưu note hiện do RecordingScreen
            // chủ động gọi asrViewModel.finishSession(...) với đầy đủ metadata
        }
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
                        // Đẩy chunk sang ASRViewModel để xử lý ASR.
                        asrViewModel?.submitSegment(event.chunk)
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

    // TODO: expose current recording file path when file saving is integrated
    val currentRecordingFilePath: String? get() = null
}