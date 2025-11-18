package com.example.notecast.domain.usecase

import com.example.notecast.domain.model.Note
import com.example.notecast.domain.repository.NoteRepository
import javax.inject.Inject // SỬA: Thêm import


class CreateNoteUseCase @Inject constructor(
    private val repo: NoteRepository
) {
    suspend operator fun invoke(note: Note) {
        repo.insertNote(note)
    }
}