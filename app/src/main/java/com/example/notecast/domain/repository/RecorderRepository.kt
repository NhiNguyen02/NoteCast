package com.example.notecast.domain.repository

import android.Manifest
import androidx.annotation.RequiresPermission
import com.example.notecast.domain.vad.VadState
import com.example.notecast.presentation.ui.record.RecordingState
import kotlinx.coroutines.flow.Flow

/**
 * Interface chuyên biệt cho việc điều khiển Ghi âm (Recorder).
 * Không dính dáng gì đến Database.
 */
interface RecorderRepository {
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