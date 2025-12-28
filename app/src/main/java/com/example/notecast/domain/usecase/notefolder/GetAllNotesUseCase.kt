package com.example.notecast.domain.usecase.notefolder

import com.example.notecast.domain.model.NoteDomain
import com.example.notecast.domain.repository.NoteRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * UseCase để lấy danh sách tất cả các ghi chú.
 */
class GetAllNotesUseCase @Inject constructor(
    private val repository: NoteRepository
) {
    operator fun invoke(): Flow<List<NoteDomain>> {
        return repository.getAllNotes()
    }
}