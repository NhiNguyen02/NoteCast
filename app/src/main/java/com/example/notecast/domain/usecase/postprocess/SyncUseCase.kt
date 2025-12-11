package com.example.notecast.domain.usecase.postprocess

import com.example.notecast.domain.repository.FolderRepository
import com.example.notecast.domain.repository.NoteRepository
import javax.inject.Inject

/**
 * UseCase cấp cao để điều phối logic đồng bộ.
 * ĐÃ CẬP NHẬT: Chỉ inject NoteRepository và FolderRepository.
 */
class SyncUseCase @Inject constructor(
    private val noteRepo: NoteRepository,
    private val folderRepo: FolderRepository
    // KHÔNG CẦN AudioRepository nữa
) {
    suspend operator fun invoke() {
        // TODO: Xử lý logic đồng bộ (ví dụ: đẩy folder trước)
        // folderRepo.syncPending()

        // Sau đó đẩy các ghi chú (bao gồm cả audio...)
        noteRepo.syncPending()
    }
}