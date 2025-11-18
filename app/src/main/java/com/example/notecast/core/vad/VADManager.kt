package com.example.notecast.core.vad

import android.content.Context
import com.example.notecast.core.device.DeviceTier
import com.example.notecast.core.device.DeviceTierDetector
import com.example.notecast.data.vad.rms.RmsVADImpl
import com.example.notecast.data.vad.silero.SileroVADImpl
import com.example.notecast.data.vad.webrtc.WebRtcVADImpl
import com.example.notecast.domain.vad.VADDetector
import com.example.notecast.domain.vad.VadState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlin.collections.copyOfRange
/**
 * VADManager:
 * - Tự động chọn VAD theo DeviceTier:
 *      LOW_END       → WebRTC VAD (hoặc RMS)
 *      MID_HIGH_END  → Silero ONNX (fallback WebRTC/RMS)
 *
 * - Tự động tạo FrameBuffer nếu frameSize không đúng.
 * - Hangover logic để tránh nhấp nháy SPEAKING/SILENT.
 */
class VADManager(
    context: Context,
    private val hangoverMs: Int = 300
) {
    private val deviceTier: DeviceTier = DeviceTierDetector.evaluate()

    private val detector: VADDetector
    private val expectedFrameSize: Int
    private val frameDurationMs: Int
    private val minSpeechMs: Int

    private val frameBuffer = FrameBuffer()
    private var silenceAccumMs = 0

    private val _state = MutableStateFlow(VadState.SILENT)
    val state: StateFlow<VadState> = _state

    init {
        when (deviceTier) {
            DeviceTier.LOW_END -> {
                detector = WebRtcVADImpl(
                    frameSize = 320,
                    sensitivity = 3,
                    minSpeechMs = 50,
                    frameDurationMs = 20
                )
                expectedFrameSize = 320
                frameDurationMs = 20
                minSpeechMs = 50
            }

            DeviceTier.MID_HIGH_END -> {
                val silero = try {
                    SileroVADImpl(
                        context = context,
                        modelPath = "models/silero_vad.onnx",
                        frameSize = 512,
                        minSpeechMs = 30,
                        frameDurationMs = 32
                    )
                } catch (t: Throwable) {
                    t.printStackTrace()
                    null
                }

                if (silero != null) {
                    detector = silero
                    expectedFrameSize = 512
                    frameDurationMs = 32
                    minSpeechMs = 30
                } else {
                    detector = RmsVADImpl(
                        frameSize = 512,
                        thresholdDb = -45.0,
                        minSpeechMs = 30,
                        frameDurationMs = 32
                    )
                    expectedFrameSize = 512
                    frameDurationMs = 32
                    minSpeechMs = 30
                }
            }
        }
        frameBuffer.configure(expectedFrameSize)
    }

    /**
     * Process PCM frame bất kỳ → gom frame đúng expectedFrameSize
     */
    fun processFrame(input: ShortArray) {
        frameBuffer.push(input) { fullFrame ->
            processExactFrame(fullFrame)
        }
    }

    /**
     * Xử lý frame đúng size → cập nhật state nội bộ
     */
    private fun processExactFrame(frame: ShortArray) {
        val isSpeech = try {
            detector.isSpeech(frame)
        } catch (t: Throwable) {
            false
        }

        if (isSpeech) {
            silenceAccumMs = 0
            if (_state.value != VadState.SPEAKING) _state.value = VadState.SPEAKING
        } else {
            silenceAccumMs += frameDurationMs
            if (silenceAccumMs >= hangoverMs && _state.value != VadState.SILENT) {
                _state.value = VadState.SILENT
            }
        }
    }

    /**
     * Stateless check frame → trả về speech/no speech, không ảnh hưởng hangover/state
     */
    fun processFrameCheck(input: ShortArray): Boolean {
        var speechDetected = false
        var idx = 0
        while (idx + expectedFrameSize <= input.size) {
            val frame = input.copyOfRange(idx, idx + expectedFrameSize)
            speechDetected = speechDetected || try {
                detector.isSpeech(frame)
            } catch (t: Throwable) { false }
            if (speechDetected) break
            idx += expectedFrameSize
        }
        return speechDetected
    }

    fun getExpectedFrameSize(): Int = expectedFrameSize
    fun getFrameDurationMs(): Int = frameDurationMs
    fun getDeviceTier(): DeviceTier = deviceTier
}


