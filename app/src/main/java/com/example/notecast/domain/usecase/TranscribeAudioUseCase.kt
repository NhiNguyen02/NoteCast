package com.example.notecast.domain.usecase

import com.example.notecast.data.local.AudioData
import com.example.notecast.data.local.Transcript
import com.example.notecast.domain.repository.ASRRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class TranscribeAudioUseCase @Inject constructor(
    private val repository: ASRRepository
) {
    suspend operator fun invoke(audioData: AudioData): Transcript {
        return repository.transcribe(audioData)
    }
}
