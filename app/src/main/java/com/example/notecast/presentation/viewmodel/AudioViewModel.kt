package com.example.notecast.presentation.viewmodel

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.notecast.domain.model.Note
import com.example.notecast.domain.repository.RecorderRepository
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
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class AudioViewModel @Inject constructor(
    private val audioRepository: RecorderRepository,
    private val startRecordingUseCase: StartRecordingUseCase,
    private val stopRecordingUseCase: StopRecordingUseCase,
    private val pauseRecordingUseCase: PauseRecordingUseCase,
    private val resumeRecordingUseCase: ResumeRecordingUseCase,
    private val getRecordingStateUseCase: GetRecordingStateUseCase,
    private val trimAndExportWavUseCase: TrimAndExportWavUseCase,
    private val transcribeAudioUseCase: TranscribeAudioUseCase,
    private val saveNoteUseCase: SaveNoteUseCase,
    private val application: Application
) : ViewModel() {

    val recordingState: StateFlow<RecordingState> = getRecordingStateUseCase().stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = RecordingState.Idle
    )

    val amplitude = audioRepository.amplitude
    val vadState = audioRepository.vadState
    val waveform = audioRepository.waveform
    val bufferAvailableSamples = audioRepository.bufferAvailableSamples

    private val _processing = MutableStateFlow(false)
    val processing: StateFlow<Boolean> = _processing

    private val _processingPercent = MutableStateFlow(0)
    val processingPercent: StateFlow<Int> = _processingPercent

    fun startRecording() = startRecordingUseCase()
    fun pauseRecording() = pauseRecordingUseCase()
    fun resumeRecording() = resumeRecordingUseCase()
    fun stopRecording() = stopRecordingUseCase()

    /**
     * Xử lý lưu file và tạo Note
     * @param folderId: ID của thư mục (nếu có)
     * @param onResult: Callback trả về Note ID mới (hoặc null nếu lỗi)
     */
    fun processAndSave(
        prePadding: Int = 1,
        postPadding: Int = 1,
        folderId: String? = null,
        onResult: (String?) -> Unit // Trả về String (Note ID)
    ) {
        viewModelScope.launch {
            _processing.value = true
            _processingPercent.value = 5

            val outDir = application.getExternalFilesDir(null) ?: File(application.filesDir, "records")
            _processingPercent.value = 10
            // Xuất file WAV
            val result = trimAndExportWavUseCase(prePadding, postPadding, outDir)
            _processingPercent.value = 30




            if (result.file != null) {
                val transcriptText = transcribeAudioUseCase(result.file)
                _processingPercent.value = 90
                // Tạo Note mới
                val newNoteId = UUID.randomUUID().toString()
                val newNote = Note(
                    id = newNoteId,
                    title = "Ghi âm ${System.currentTimeMillis()}", // Tạm thời
                    noteType = "VOICE",
                    filePath = result.file.absolutePath, // Lưu đường dẫn file
                    durationMs = result.recordedMs,
                    rawText = transcriptText, // Placeholder cho STT
                    updatedAt = System.currentTimeMillis(),
                    createdAt = 0,
                    folderId = folderId,
                    content = transcriptText
                )

                // 3. Lưu vào Database
                saveNoteUseCase(newNote)

                _processingPercent.value = 100
                delay(500)
                _processing.value = false

                // 4. Trả về ID để điều hướng
                onResult(newNoteId)
            } else {
                _processing.value = false
                onResult(null) // Lỗi
            }
        }
    }
}