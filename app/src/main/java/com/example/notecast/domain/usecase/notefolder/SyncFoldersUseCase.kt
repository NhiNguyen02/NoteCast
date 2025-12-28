package com.example.notecast.domain.usecase.notefolder

import com.example.notecast.data.repository.FolderRepositoryImpl
import javax.inject.Inject

/**
 * Use case để đồng bộ toàn bộ danh sách folder từ NoteServices về Room.
 *
 * - Gọi NoteServicesAPI /folders.
 * - Merge về Room thông qua FolderRepositoryImpl.syncFromRemoteOnce().
 * - Room là source-of-truth, ViewModel chỉ cần collect GetAllFoldersUseCase().
 */
class SyncFoldersUseCase @Inject constructor(
    // Dùng FolderRepositoryImpl vì syncFromRemoteOnce chưa có trong interface FolderRepository.
    private val folderRepositoryImpl: FolderRepositoryImpl
) {
    suspend operator fun invoke() {
        folderRepositoryImpl.syncFromRemoteOnce()
    }
}
