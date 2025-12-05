package com.example.notecast.core.audio

import android.Manifest
import androidx.annotation.RequiresPermission
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

/**
 * AudioEngine chịu trách nhiệm:
 * - Khởi tạo AudioRecord 16 kHz, mono, PCM_16BIT thông qua AudioRecorder
 * - Đọc audio liên tục trên background thread
 * - Đẩy PCM frames tới 3 buffer: VAD, ASR, Recorder
 *
 * Engine này không biết gì về VAD/ASR cụ thể, chỉ publish PCM.
 */
@Singleton
class AudioEngine @Inject constructor(
    private val recorder: AudioRecorder,
    private val vadBuffer: AudioBuffer<ShortArray>,
    private val asrBuffer: AudioBuffer<ShortArray>,
    private val recorderBuffer: AudioBuffer<ShortArray>,
) {

    private val scope = CoroutineScope(Dispatchers.IO)
    private var recordingJob: Job? = null

    @RequiresPermission(Manifest.permission.RECORD_AUDIO)
    @Synchronized
    fun start(onPcmFrame: ((ShortArray) -> Unit)? = null) {
        if (recordingJob?.isActive == true) return

        // đảm bảo buffer rỗng trước khi bắt đầu session mới
        // (tuỳ strategy, có thể bỏ nếu muốn giữ preroll)
        // Ở đây clear để tránh rò rỉ data giữa các session.
        if (vadBuffer is ClearableBuffer) vadBuffer.clear()
        if (asrBuffer is ClearableBuffer) asrBuffer.clear()
        if (recorderBuffer is ClearableBuffer) recorderBuffer.clear()

        recordingJob = scope.launch {
            recorder.start { frame ->
                // Nhân bản frame vì AudioRecord thường reuse buffer nội bộ
                val copy = frame.copyOf()
                // Tuỳ chính sách drop/backpressure bên trong buffer
                vadBuffer.write(copy)
                asrBuffer.write(copy)
                recorderBuffer.write(copy)

                // publish ra ngoài nếu có callback
                onPcmFrame?.invoke(copy)
            }
        }
    }

    suspend fun stop() {
        // dừng thu và chờ job hoàn tất
        recorder.stop()
        recordingJob?.cancelAndJoin()
        recordingJob = null

        // giải phóng dung lượng buffer khi không còn dùng tới (nếu hỗ trợ clear)
        if (vadBuffer is ClearableBuffer) vadBuffer.clear()
        if (asrBuffer is ClearableBuffer) asrBuffer.clear()
        if (recorderBuffer is ClearableBuffer) recorderBuffer.clear()
    }

    fun pause() {
        recorder.stop()
    }

    @RequiresPermission(Manifest.permission.RECORD_AUDIO)
    fun resume() {
        // Resume = start lại, để ViewModel quyết định semantic
        start()
    }
}
