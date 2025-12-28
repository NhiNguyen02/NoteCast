package com.example.notecast.domain.usecase.notefolder

import com.example.notecast.domain.model.NoteDomain
import com.example.notecast.domain.repository.NoteRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * UseCase để lấy danh sách các ghi chú theo một thư mục cụ thể.
 */
class GetNotesByFolderUseCase @Inject constructor(
    private val repository: NoteRepository
) {
    operator fun invoke(folderId: String): Flow<List<NoteDomain>> {
        return repository.getNotesByFolder(folderId)
    }
}