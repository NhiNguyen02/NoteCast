package com.example.notecast.domain.usecase

import javax.inject.Inject

/**
 * ProcessAudioUseCase
 *
 * Pipeline chunker cho audio offline/dài:
 * - Nhận toàn bộ PCM FloatArray 16kHz mono từ nguồn (file/memory).
 * - Nếu audio ngắn (< 30s) -> gọi TranscribeChunkUseCase một lần.
 * - Nếu dài hơn -> chia thành các chunk 30s, stride 5s, gọi TranscribeChunkUseCase cho từng chunk.
 * - Tạm thời ghép kết quả bằng joinToString(" "); sau này có thể thay bằng MergeChunksUseCase.
 */
class ProcessAudioUseCase @Inject constructor(
    private val transcribeChunkUseCase: TranscribeChunkUseCase,
    private val sampleRate: Int = 16_000,
    private val chunkLengthSec: Int = 30,
    private val strideSec: Int = 5,
) {

    /**
     * @param fullAudio PCM FloatArray 16kHz mono cho toàn bộ audio.
     * @return transcript text cho toàn bộ audio (đã ghép các chunk).
     */
    suspend operator fun invoke(fullAudio: FloatArray): String {
        if (fullAudio.isEmpty()) return ""

        val chunkSamples = chunkLengthSec * sampleRate
        val strideSamples = strideSec * sampleRate

        // Audio ngắn hơn hoặc bằng 30s: xử lý một lần.
        if (fullAudio.size <= chunkSamples) {
            return transcribeChunkUseCase(fullAudio)
        }

        // Audio dài hơn 30s: cắt thành các chunk 30s, stride 5s.
        val texts = mutableListOf<String>()
        var start = 0
        val n = fullAudio.size

        while (start < n) {
            val end = minOf(n, start + chunkSamples)
            val chunk = fullAudio.copyOfRange(start, end)
            val text = transcribeChunkUseCase(chunk)
            if (text.isNotBlank()) {
                texts += text
            }
            if (end == n) break
            start += strideSamples
        }

        // TODO: thay join thuần bằng MergeChunksUseCase để xử lý overlap 5s thông minh hơn.
        return texts.joinToString(separator = " ")
    }
}