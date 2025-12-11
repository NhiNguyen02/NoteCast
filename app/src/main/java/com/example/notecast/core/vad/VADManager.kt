package com.example.notecast.core.vad

import com.example.notecast.data.vad.rms.RmsVADImpl
// import com.example.notecast.data.vad.silero.SileroVADImpl
import com.example.notecast.domain.vad.VADDetector

/**
 * VADManager: tạm thời dùng RMS VAD cho mọi device để tránh lỗi ONNX runtime với Silero.
 *
 * Logic Silero + device tier cũ được giữ lại dưới dạng comment để có thể dùng lại khi cần.
 */
class VADManager() {
    val detector: VADDetector
    private val expectedFrameSize: Int
    private val frameDurationMs: Int

    init {
        // ==== Tạm thời: dùng RMS VAD cho tất cả thiết bị ====
        detector = RmsVADImpl(
            frameSize = 320,
            thresholdDb = -45.0,
            minSpeechMs = 50,
            frameDurationMs = 20
        )
        expectedFrameSize = 320
        frameDurationMs = 20

        // ==== Logic Silero cho mọi thiết bị (gây lỗi ORT Missing Input: sr), để lại để debug sau ====
        // val silero = try {
        //     SileroVADImpl(
        //         context = context,
        //         modelPath = "silero_vad.onnx",
        //         frameSize = 512,
        //         minSpeechMs = 30,
        //         frameDurationMs = 32
        //     )
        // } catch (t: Throwable) {
        //     t.printStackTrace()
        //     null
        // }
        // if (silero != null) {
        //     detector = silero
        //     expectedFrameSize = 512
        //     frameDurationMs = 32
        // } else {
        //     detector = RmsVADImpl(
        //         frameSize = 320,
        //         thresholdDb = -45.0,
        //         minSpeechMs = 50,
        //         frameDurationMs = 20
        //     )
        //     expectedFrameSize = 320
        //     frameDurationMs = 20
        // }

        // ==== Logic cũ theo device tier (chỉ tham khảo, hiện đang không dùng) ====
        // val deviceTier: DeviceTier = DeviceTierDetector.evaluate()
        // when (deviceTier) { ... }
    }
}
