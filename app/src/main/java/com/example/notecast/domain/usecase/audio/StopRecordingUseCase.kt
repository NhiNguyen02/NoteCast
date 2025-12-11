package com.example.notecast.domain.usecase.audio

import com.example.notecast.domain.repository.AudioRepository
import javax.inject.Inject

class StopRecordingUseCase @Inject constructor(
    private val repo: AudioRepository
) {
    suspend operator fun invoke() {
        repo.stopRecording()
    }
}
