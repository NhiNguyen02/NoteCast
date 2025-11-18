package com.example.notecast.domain.usecase

import com.example.notecast.domain.repository.AudioRepository
import javax.inject.Inject

class GetRecordingStateUseCase @Inject constructor(
    private val repo: AudioRepository
) {
    operator fun invoke() = repo.recordingState
}
