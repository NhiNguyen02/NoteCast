package com.example.notecast.domain.usecase

import com.example.notecast.domain.repository.RecorderRepository
import javax.inject.Inject

class ResumeRecordingUseCase @Inject constructor(
    private val repo: RecorderRepository
) {
    operator fun invoke() {
        repo.resumeRecording()
    }
}
