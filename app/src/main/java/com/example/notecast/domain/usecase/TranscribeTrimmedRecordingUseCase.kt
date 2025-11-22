package com.example.notecast.domain.usecase

import com.example.notecast.data.local.AudioData
import com.example.notecast.data.local.Transcript
import javax.inject.Inject
import kotlin.collections.isEmpty

class TranscribeTrimmedRecordingUseCase @Inject constructor(
    private val getTrimmedRecordingUseCase: GetTrimmedRecordingUseCase,
    private val transcribeAudioUseCase: TranscribeAudioUseCase
) {
    suspend operator fun invoke(prePadding: Int = 1, postPadding: Int = 1): Transcript {
        val pcm = getTrimmedRecordingUseCase(prePadding, postPadding)
        if (pcm.isEmpty()) return Transcript(text = "", timestamp = System.currentTimeMillis())
        return transcribeAudioUseCase(AudioData(pcm, 16000))
    }

    /**
     * Optionally, allow direct PCM → transcription
     */
    suspend fun transcribeDirect(pcm: ShortArray, sampleRate: Int = 16000): Transcript {
        if (pcm.isEmpty()) return Transcript(text = "", timestamp = System.currentTimeMillis())
        return transcribeAudioUseCase(AudioData(pcm, sampleRate))
    }
}

