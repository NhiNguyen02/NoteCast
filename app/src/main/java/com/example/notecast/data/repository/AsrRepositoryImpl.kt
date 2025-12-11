package com.example.notecast.data.repository

import com.example.notecast.data.remote.PhoWhisperApiService
import com.example.notecast.data.remote.dto.PhoWhisperTranscribeUrlRequest
import com.example.notecast.domain.model.AsrResult
import com.example.notecast.domain.model.ChunkResult
import com.example.notecast.domain.repository.AsrRepository
import javax.inject.Inject

/**
 * AsrRepositoryImpl
 *
 * Triển khai AsrRepository sử dụng backend PhoWhisper (HF Space) qua PhoWhisperApiService.
 * Thay thế pipeline ONNX local bằng gọi API từ URL audio.
 */
class AsrRepositoryImpl @Inject constructor(
    private val api: PhoWhisperApiService
) : AsrRepository {

    override suspend fun transcribeByUrl(audioUrl: String): AsrResult {
        val request = PhoWhisperTranscribeUrlRequest(audioUrl = audioUrl)
        val response = api.transcribeUrl(request)

        val chunks = response.chunks.map {
            ChunkResult(
                startSec = it.start,
                endSec = it.end,
                text = it.text
            )
        }

        return AsrResult(
            text = response.text,
            durationSec = response.duration,
            sampleRate = response.sampleRate,
            chunks = chunks
        )
    }
}

