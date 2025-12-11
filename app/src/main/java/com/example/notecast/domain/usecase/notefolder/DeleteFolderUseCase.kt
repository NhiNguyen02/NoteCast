package com.example.notecast.domain.usecase.notefolder

import com.example.notecast.domain.repository.FolderRepository
import javax.inject.Inject

/**
 * UseCase để "xóa mềm" (soft delete) một thư mục.
 */
class DeleteFolderUseCase @Inject constructor(
    private val repository: FolderRepository
) {
    suspend operator fun invoke(folderId: String) {
        if (folderId.isNotBlank()) {
            repository.deleteFolder(folderId)
        }
    }
}