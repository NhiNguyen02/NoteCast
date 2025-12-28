package com.example.notecast.domain.usecase.notefolder

import com.example.notecast.domain.model.NoteDomain
import com.example.notecast.domain.repository.NoteRemoteRepository
import javax.inject.Inject

/**
 * Use case: Tạo note trên backend NoteServices.
 * Dùng khi cần đảm bảo note tồn tại trên server trước khi gọi các API khác (vd: regenerate).
 */
class CreateNoteOnBackendUseCase @Inject constructor(
    private val noteRemoteRepository: NoteRemoteRepository
) {
    /**
     * @param note Note cần tạo trên backend
     * @param generateTasks Danh sách task cần generate (normalize, keywords, summary, mindmap)
     */
    suspend operator fun invoke(note: NoteDomain, generateTasks: List<String> = emptyList()) {
        noteRemoteRepository.createNote(note, generateTasks)
    }
}

