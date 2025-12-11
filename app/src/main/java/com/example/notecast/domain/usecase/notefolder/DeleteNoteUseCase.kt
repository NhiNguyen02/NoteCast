package com.example.notecast.domain.usecase.notefolder

import com.example.notecast.domain.repository.NoteRepository
import javax.inject.Inject

/**
 * UseCase để "xóa mềm" (soft delete) một ghi chú.
 */
class DeleteNoteUseCase @Inject constructor(
    private val repository: NoteRepository
) {
    suspend operator fun invoke(noteId: String) {
        if (noteId.isNotBlank()) {
            repository.deleteNote(noteId)
        }
    }
}