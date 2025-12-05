package com.example.notecast.domain.usecase

import com.example.notecast.domain.repository.AudioRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/**
 * StreamAudioUseCase
 * - Đọc PCM16 từ AudioRepository
 * - Chuẩn hóa về Float[-1f, 1f] @16kHz (giữ nguyên sample rate, vì engine đã 16k).
 */
class StreamAudioUseCase @Inject constructor(
    private val repo: AudioRepository,
) {
    operator fun invoke(): Flow<FloatArray> {
        return repo.streamPcmFrames().map { pcm ->
            val out = FloatArray(pcm.size)
            for (i in pcm.indices) {
                out[i] = pcm[i] / 32768f
            }
            out
        }
    }
}

