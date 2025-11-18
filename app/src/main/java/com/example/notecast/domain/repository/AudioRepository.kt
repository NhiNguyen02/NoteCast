package com.example.notecast.domain.repository

import com.example.notecast.domain.vad.VadState
import com.example.notecast.presentation.ui.record.RecordingState
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

}