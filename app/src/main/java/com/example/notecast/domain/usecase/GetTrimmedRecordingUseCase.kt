package com.example.notecast.domain.usecase


import com.example.notecast.domain.repository.RecorderRepository
import javax.inject.Inject

class GetTrimmedRecordingUseCase @Inject constructor(
    private val repo: RecorderRepository
) {
    suspend operator fun invoke(prePaddingChunks: Int = 1, postPaddingChunks: Int = 1): ShortArray {
        return repo.getTrimmedRecording(prePaddingChunks, postPaddingChunks)
    }
}
