package com.example.notecast.domain.usecase

import com.example.notecast.domain.repository.RecorderRepository
import javax.inject.Inject

class StopRecordingUseCase @Inject constructor(
    private val repo: RecorderRepository
) {
    operator fun invoke() {
        repo.stopRecording()
    }
}
