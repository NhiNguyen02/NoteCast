package com.example.notecast.domain.usecase.notefolder

import com.example.notecast.domain.model.NoteDomain
import com.example.notecast.domain.repository.NoteRemoteRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Use case: quan sát trạng thái note từ NoteServices (qua SSE /notes/{id}/events + GET /notes/{id}).
 * Trả về Flow<NoteDomain> domain thuần cho ViewModel.
 */
class ObserveRemoteNoteUseCase @Inject constructor(
    private val remoteRepository: NoteRemoteRepository,
) {
    operator suspend fun invoke(noteId: String): Flow<NoteDomain> {
        return remoteRepository.observeRemoteNote(noteId)
    }
}

