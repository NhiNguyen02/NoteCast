package com.example.notecast.domain.repository

import com.example.notecast.domain.vad.VadState
import com.example.notecast.presentation.ui.record.RecordingState
import com.example.notecast.domain.model.Audio
import kotlinx.coroutines.flow.Flow

interface AudioRepository {
    val recordingState: Flow<RecordingState>
    val amplitude: Flow<Float>
    val vadState: Flow<VadState>
    val lastPcmFrame: Flow<ShortArray?>
    val waveform: Flow<List<Float>>
    val bufferAvailableSamples: Flow<Int>

    fun startRecording()
    fun stopRecording()
    fun pauseRecording()
    fun resumeRecording()
    fun isRecording(): Boolean
    fun getBufferAvailableSamples(): Int
    suspend fun getTrimmedRecording(prePaddingChunks: Int = 1, postPaddingChunks: Int = 1): ShortArray

    fun allAudio(): Flow<List<Audio>>
    suspend fun getAudioById(id: String): Audio?
    suspend fun insertAudio(audio: Audio)
    suspend fun updateAudio(audio: Audio)
    suspend fun deleteAudio(id: String)
    suspend fun uploadPendingAudio() // orchestration entrypoint if repo handles upload
}

