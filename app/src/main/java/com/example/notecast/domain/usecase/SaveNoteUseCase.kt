package com.example.notecast.domain.usecase

import com.example.notecast.domain.model.Note
import com.example.notecast.domain.repository.NoteRepository
import java.util.UUID
import javax.inject.Inject

/**
 * UseCase để lưu (Tạo mới HOẶC Cập nhật) một ghi chú.
 * Nơi chứa business logic (ví dụ: tạo ID, set timestamp).
 */
class SaveNoteUseCase @Inject constructor(
    private val repository: NoteRepository
) {
    suspend operator fun invoke(note: Note) {
        // Đây là business logic (logic nghiệp vụ)
        val noteToSave = note.copy(
            // Nếu ID rỗng (tạo mới), gán UUID mới
            id = if (note.id.isBlank()) UUID.randomUUID().toString() else note.id,
            // Luôn cập nhật timestamp khi lưu
            updatedAt = System.currentTimeMillis()
        )
        // Gọi hàm 'saveNote' MỚI của Repository
        repository.saveNote(noteToSave)
    }
}