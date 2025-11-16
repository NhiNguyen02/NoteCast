package com.example.notecast.domain.usecase

import com.example.notecast.domain.repository.AudioRepository
import com.example.notecast.domain.repository.NoteRepository
import javax.inject.Inject // SỬA: Thêm import

/**
 * High-level usecase to orchestrate sync.
 */
// SỬA: Thêm @Inject constructor
class SyncUseCase @Inject constructor(
    private val audioRepo: AudioRepository,
    private val noteRepo: NoteRepository
) {
    suspend operator fun invoke() {
        // Simple orchestration: audio first, then notes
        audioRepo.uploadPendingAudio()
        noteRepo.syncPending()
    }
}