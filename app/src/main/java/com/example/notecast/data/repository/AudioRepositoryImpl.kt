package com.example.notecast.data.repository

import android.Manifest
import androidx.annotation.RequiresPermission
import com.example.notecast.core.audio.AudioEngine
import com.example.notecast.domain.repository.AudioRepository
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import javax.inject.Inject

/**
 * Implementation của AudioRepository sử dụng AudioEngine.
 */
class AudioRepositoryImpl @Inject constructor(
    private val audioEngine: AudioEngine,
) : AudioRepository {

    /**
     * SharedFlow dùng để publish PCM frames từ AudioEngine ra domain.
     * replay = 0, bufferCapacity một vài frame, drop oldest để ưu tiên độ trễ thấp.
     */
    private val pcmFramesFlow = MutableSharedFlow<ShortArray>(
        replay = 0,
        extraBufferCapacity = 8,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )

    override fun streamPcmFrames(): Flow<ShortArray> = pcmFramesFlow

    override fun asrPcmFrames(): Flow<ShortArray> = pcmFramesFlow

    @RequiresPermission(Manifest.permission.RECORD_AUDIO)
    override fun startRecording() {
        audioEngine.start { frame ->
            // emit non-blocking, nếu buffer đầy sẽ drop oldest theo chính sách trên
            pcmFramesFlow.tryEmit(frame)
        }
    }

    override suspend fun stopRecording() {
        audioEngine.stop()
    }

    override fun pauseRecording() {
        audioEngine.pause()
    }

    @RequiresPermission(Manifest.permission.RECORD_AUDIO)
    override fun resumeRecording() {
        audioEngine.resume()
    }
}
