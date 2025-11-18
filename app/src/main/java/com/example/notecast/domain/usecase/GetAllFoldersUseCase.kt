package com.example.notecast.domain.usecase

import com.example.notecast.domain.model.Folder
import com.example.notecast.domain.repository.FolderRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * UseCase để lấy danh sách tất cả các thư mục.
 * ViewModel sẽ .collect() Flow này để tự động cập nhật UI.
 */
class GetAllFoldersUseCase @Inject constructor(
    private val repository: FolderRepository
) {
    operator fun invoke(): Flow<List<Folder>> {
        return repository.getAllFolders()
    }
}