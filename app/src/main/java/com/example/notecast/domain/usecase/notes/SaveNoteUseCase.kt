package com.example.notecast.domain.usecase.notes

import com.example.notecast.domain.model.Note // SỬ DỤNG MODEL MỚI
import com.example.notecast.domain.repository.NoteRepository
import javax.inject.Inject

/**
 * Use Case để xử lý logic lưu (hoặc cập nhật) một ghi chú.
 * Business Logic: Đảm bảo ghi chú có tiêu đề, cập nhật ngày sửa cuối cùng, gọi Repository.
 */
class SaveNoteUseCase @Inject constructor(
    private val repository: NoteRepository
) {
    suspend operator fun invoke(note: Note) {
        if (note.title.isBlank()) {
            throw IllegalArgumentException("Note title cannot be empty.")
        }

        // Cập nhật ngày chỉnh sửa cuối cùng
        val noteToSave = note.copy(lastEdited = java.util.Date())

        repository.saveNote(noteToSave)
    }
}