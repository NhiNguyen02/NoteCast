package com.example.notecast.domain.usecase

import com.example.notecast.domain.model.Note
import com.example.notecast.domain.repository.NoteRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetNoteByIdUseCase @Inject constructor(
    private val repository: NoteRepository
) {
    /**
     * Lấy 1 ghi chú VÀ tự động cập nhật UI khi nó thay đổi
     */
    operator fun invoke(noteId: String): Flow<Note?> {
        if (noteId.isBlank()) {
            return kotlinx.coroutines.flow.flowOf(null) // Trả về Flow null nếu ID rỗng
        }
        // ViewModel sẽ .collect() Flow này
        return repository.getNoteById(noteId)
    }
}