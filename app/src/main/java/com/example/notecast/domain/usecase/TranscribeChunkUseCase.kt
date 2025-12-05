package com.example.notecast.domain.usecase

import android.util.Log
import com.example.notecast.core.asr.MelProcessor
import com.example.notecast.core.asr.PhoWhisperDecoderONNX
import com.example.notecast.core.asr.PhoWhisperEncoderONNX
import javax.inject.Inject
import javax.inject.Named
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

private const val TAG_TRANSCRIBE = "TranscribeChunkUseCase"

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
    @Named("IO") private val ioDispatcher: CoroutineDispatcher,
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
    suspend operator fun invoke(chunk: FloatArray): String = withContext(ioDispatcher) {
        Log.d(TAG_TRANSCRIBE, "invoke: samples=${chunk.size}")

        // 1. PCM -> log-mel [80, T]
        val (mel, nFrames) = melProcessor.computeLogMel(chunk)
        Log.d(TAG_TRANSCRIBE, "mel computed: nFrames=$nFrames, melSize=${mel.size}")
        if (nFrames == 0) return@withContext ""

        val usedFrames = if (nFrames <= maxMelFrames) {
            nFrames
        } else {
            Log.w(TAG_TRANSCRIBE, "nFrames=$nFrames > $maxMelFrames, trimming")
            maxMelFrames
        }

        val paddedMelSize = 80 * maxMelFrames
        val melPadded = FloatArray(paddedMelSize) { 0f }
        val copyFrames = minOf(nFrames, maxMelFrames)
        val copyFloats = 80 * copyFrames
        mel.copyInto(destination = melPadded, endIndex = copyFloats)
        Log.d(TAG_TRANSCRIBE, "melPaddedSize=$paddedMelSize, copyFrames=$copyFrames, usedFrames=$usedFrames")

        // Đảm bảo ONNXRuntimeManager đã init trên IO trước khi dùng encoderSession
        val encoderOut = encoder.runWithInit(melPadded, usedFrames)
        Log.d(TAG_TRANSCRIBE, "encoderOut: length=${encoderOut.length}, hiddenSize=${encoderOut.hiddenStates.size}")

        val text = decoder.runAutoregressive(encoderOut)
        Log.d(TAG_TRANSCRIBE, "decoder output text='${text.take(120)}'")
        text
    }
}
