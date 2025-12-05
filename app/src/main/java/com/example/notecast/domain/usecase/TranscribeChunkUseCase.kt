package com.example.notecast.domain.usecase

import com.example.notecast.core.asr.MelProcessor
import com.example.notecast.core.asr.PhoWhisperDecoderONNX
import com.example.notecast.core.asr.PhoWhisperEncoderONNX
import javax.inject.Inject

/**
 * TranscribeChunkUseCase
 * - Nhận 1 chunk PCM float 16kHz từ Segmenter (thường < 30s).
 * - Tính mel (log-mel [80, T]).
 * - Chuẩn hóa T theo giới hạn encoder (T <= 3000 mel frames ~ 30s).
 * - Chạy encoder & decoder PhoWhisper ONNX và trả về text.
 */
class TranscribeChunkUseCase @Inject constructor(
    private val melProcessor: MelProcessor,
    private val encoder: PhoWhisperEncoderONNX,
    private val decoder: PhoWhisperDecoderONNX,
) {

    /**
     * Số mel frames tối đa PhoWhisper encoder hỗ trợ cho 1 lần gọi.
     * Theo docs: 30s @ hop 10 ms -> 3000 frames.
     */
    private val maxMelFrames: Int = 3000

    /**
     * @param chunk PCM FloatArray 16kHz mono (ASR chunk từ Segmenter).
     * @return transcript text cho chunk này.
     */
    suspend operator fun invoke(chunk: FloatArray): String {
        // 1. PCM -> log-mel [80, T]
        val (mel, nFrames) = melProcessor.computeLogMel(chunk)
        if (nFrames == 0) return ""

        // 2. Chuẩn hóa T với giới hạn encoder.
        //    - Nếu T <= 3000: truyền trực tiếp [1, 80, T].
        //    - Nếu T > 3000: tạm thời cắt tail xuống 3000 frame đầu.
        //      (Sliding window/overlap cho >30s sẽ được xử lý ở tầng cao hơn nếu cần.)
        val usedFrames = if (nFrames <= maxMelFrames) nFrames else maxMelFrames
        val melForEncoder = if (nFrames <= maxMelFrames) {
            mel
        } else {
            // Cắt mảng mel flatten [80 * nFrames] về [80 * usedFrames]
            mel.copyOf(usedFrames * 80)
        }

        // 3. Chạy encoder ONNX với [1, 80, usedFrames]
        val encoderOut = encoder.run(melForEncoder, usedFrames)

        // 4. Chạy decoder autoregressive để lấy text
        return decoder.runAutoregressive(encoderOut)
    }
}
