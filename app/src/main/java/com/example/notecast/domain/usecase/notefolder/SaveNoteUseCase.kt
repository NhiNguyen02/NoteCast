package com.example.notecast.domain.usecase.notefolder

import com.example.notecast.domain.model.NoteDomain
import com.example.notecast.domain.repository.NoteRepository
import javax.inject.Inject

class SaveNoteUseCase @Inject constructor(
    private val noteRepository: NoteRepository,
) {
    suspend operator fun invoke(note: NoteDomain) {
        noteRepository.saveNote(note)
    }
}