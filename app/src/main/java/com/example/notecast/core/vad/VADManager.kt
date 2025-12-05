package com.example.notecast.core.vad

import android.content.Context
import com.example.notecast.core.device.DeviceTier
import com.example.notecast.core.device.DeviceTierDetector
import com.example.notecast.data.vad.rms.RmsVADImpl
import com.example.notecast.data.vad.silero.SileroVADImpl
import com.example.notecast.data.vad.webrtc.WebRtcVADImpl
import com.example.notecast.domain.vad.VADDetector

/**
 * VADManager tối giản: chỉ giữ lại logic chọn detector theo device tier.
 * Không còn quản lý state ghi âm, hangover hay flow VadState.
 */
class VADManager(
    context: Context,
//    private val hangoverMs: Int = 300 // giữ tham số nếu cần tái sử dụng sau này
) {
    private val deviceTier: DeviceTier = DeviceTierDetector.evaluate()

    val detector: VADDetector
    private val expectedFrameSize: Int
    private val frameDurationMs: Int
//    private val minSpeechMs: Int

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
//                minSpeechMs = 50
            }

            DeviceTier.MID_HIGH_END -> {
                val silero = try {
                    SileroVADImpl(
                        context = context,
                        modelPath = "silero_vad.onnx",
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
//                    minSpeechMs = 30
                } else {
                    detector = RmsVADImpl(
                        frameSize = 512,
                        thresholdDb = -45.0,
                        minSpeechMs = 30,
                        frameDurationMs = 32
                    )
                    expectedFrameSize = 512
                    frameDurationMs = 32
//                    minSpeechMs = 30
                }
            }
        }
    }

//    fun getExpectedFrameSize(): Int = expectedFrameSize
//    fun getFrameDurationMs(): Int = frameDurationMs
//    fun getDeviceTier(): DeviceTier = deviceTier
}
