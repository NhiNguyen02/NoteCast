package com.example.notecast.domain.usecase.notefolder

import com.example.notecast.domain.model.Folder
import com.example.notecast.domain.repository.FolderRepository
import java.util.UUID
import javax.inject.Inject

/**
 * UseCase để lưu (Tạo mới hoặc Cập nhật) một thư mục.
 */
class SaveFolderUseCase @Inject constructor(
    private val repository: FolderRepository
) {
    suspend operator fun invoke(folder: Folder) {
        // Business logic: Gán ID nếu tạo mới và cập nhật timestamp
        val folderToSave = folder.copy(
            id = folder.id.ifBlank { UUID.randomUUID().toString() },
            updatedAt = System.currentTimeMillis()
        )
        repository.saveFolder(folderToSave)
    }
}