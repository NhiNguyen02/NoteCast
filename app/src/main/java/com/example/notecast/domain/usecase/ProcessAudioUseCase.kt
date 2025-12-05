package com.example.notecast.domain.usecase

import com.example.notecast.domain.model.ChunkResult
import javax.inject.Inject

/**
 * ProcessAudioUseCase
 *
 * Pipeline chunker cho audio offline/dài:
 * - Nhận toàn bộ PCM FloatArray 16kHz mono từ nguồn (file/memory).
 * - Nếu audio ngắn (< 30s) -> trả về 1 ChunkResult duy nhất.
 * - Nếu dài hơn -> chia thành các chunk 30s, stride 5s, trả về danh sách ChunkResult để MergeChunksUseCase xử lý.
 */
class ProcessAudioUseCase @Inject constructor(
    private val transcribeChunkUseCase: TranscribeChunkUseCase,
    private val sampleRate: Int = 16_000,
    private val chunkLengthSec: Int = 30,
    private val strideSec: Int = 5,
) {

    /**
     * @param fullAudio PCM FloatArray 16kHz mono cho toàn bộ audio.
     * @return Danh sách ChunkResult (startSec, endSec, text) để dùng cho MergeChunksUseCase.
     */
    suspend operator fun invoke(fullAudio: FloatArray): List<ChunkResult> {
        if (fullAudio.isEmpty()) return emptyList()

        val chunkSamples = chunkLengthSec * sampleRate
        val strideSamples = strideSec * sampleRate

        // Audio ngắn hơn hoặc bằng 30s: xử lý một lần.
        if (fullAudio.size <= chunkSamples) {
            val text = transcribeChunkUseCase(fullAudio)
            val durationSec = fullAudio.size.toDouble() / sampleRate
            return if (text.isBlank()) emptyList() else listOf(
                ChunkResult(
                    startSec = 0.0,
                    endSec = durationSec,
                    text = text,
                )
            )
        }

        // Audio dài hơn 30s: cắt thành các chunk 30s, stride 5s.
        val results = mutableListOf<ChunkResult>()
        var start = 0
        val n = fullAudio.size

        while (start < n) {
            val end = minOf(n, start + chunkSamples)
            val chunk = fullAudio.copyOfRange(start, end)
            val text = transcribeChunkUseCase(chunk)
            if (text.isNotBlank()) {
                val startSec = start.toDouble() / sampleRate
                val endSec = end.toDouble() / sampleRate
                results += ChunkResult(
                    startSec = startSec,
                    endSec = endSec,
                    text = text,
                )
            }
            if (end == n) break
            start += strideSamples
        }

        return results
    }
}