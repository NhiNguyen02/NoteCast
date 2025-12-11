package com.example.notecast.domain.usecase.audio

import com.example.notecast.domain.repository.AudioRepository
import javax.inject.Inject

class ResumeRecordingUseCase @Inject constructor(
    private val repo: AudioRepository
) {
    operator fun invoke() {
        repo.resumeRecording()
    }
}
