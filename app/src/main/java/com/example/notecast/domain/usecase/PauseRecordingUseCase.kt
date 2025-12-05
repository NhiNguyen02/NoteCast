package com.example.notecast.domain.usecase

import com.example.notecast.domain.repository.AudioRepository
import javax.inject.Inject

class PauseRecordingUseCase @Inject constructor(
    private val repo: AudioRepository
) {
    operator fun invoke() {
        repo.pauseRecording()
    }
}
