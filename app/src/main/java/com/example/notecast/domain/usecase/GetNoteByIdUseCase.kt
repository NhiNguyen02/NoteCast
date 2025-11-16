package com.example.notecast.domain.usecase

import com.example.notecast.domain.model.Note
import com.example.notecast.domain.repository.NoteRepository
import javax.inject.Inject

/**
 * Use Case để tải một ghi chú cụ thể theo ID.
 * Cần thiết cho NoteEditViewModel.
 */
class GetNoteByIdUseCase @Inject constructor(
    private val repository: NoteRepository
) {
    suspend operator fun invoke(noteId: String): Note? {
        if (noteId.isBlank()) {
            return null
        }
        return repository.getNoteById(noteId)
    }
}