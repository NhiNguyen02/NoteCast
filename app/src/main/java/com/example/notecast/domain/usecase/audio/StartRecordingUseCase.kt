package com.example.notecast.domain.usecase.audio

import com.example.notecast.domain.repository.AudioRepository
import javax.inject.Inject

class StartRecordingUseCase @Inject constructor(
    private val repo: AudioRepository
) {
    operator fun invoke() {
        repo.startRecording()
    }
}
