package com.example.notecast.domain.usecase

import com.example.notecast.domain.repository.RecorderRepository
import javax.inject.Inject

class StartRecordingUseCase @Inject constructor(
    private val repo: RecorderRepository
) {
    operator fun invoke() {
        repo.startRecording()
    }
}
