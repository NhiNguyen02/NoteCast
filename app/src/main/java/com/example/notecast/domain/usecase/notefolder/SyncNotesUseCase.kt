package com.example.notecast.domain.usecase.notefolder

import com.example.notecast.domain.repository.NoteRepository
import javax.inject.Inject

/**
 * Use case để đồng bộ danh sách note từ NoteServices về Room.
 *
 * - Gọi NoteRemoteRepository.fetchAllNotes() thông qua NoteRepository.syncPending().
 * - Ghi đè/merge vào Room.
 * - Room là source-of-truth, UI chỉ cần collect GetAllNotesUseCase().
 */
class SyncNotesUseCase @Inject constructor(
    private val noteRepository: NoteRepository,
) {
    suspend operator fun invoke() {
        noteRepository.syncPending()
    }
}

