package com.example.notecast.domain.usecase.postprocess

import com.example.notecast.domain.model.NoteDomain
import com.example.notecast.domain.model.NoteStatus
import com.example.notecast.domain.repository.NoteRemoteRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import retrofit2.HttpException
import javax.inject.Inject

/**
 * RegenerateNoteUseCase
 *
 * Dùng NoteServices API để trigger enrichment cho một note (normalize, keywords, summary, mindmap)
 * và poll kết quả trực tiếp từ NoteServices, sau đó trả về NoteDomain duy nhất.
 */
class RegenerateNoteUseCase @Inject constructor(
    private val noteRemoteRepository: NoteRemoteRepository,
) {
    /**
     * @param noteId  id của note trên backend (chính là NoteDomain.id)
     * @param generate danh sách task cần chạy, ví dụ ["normalize","keywords","summary","mindmap"].
     */
    operator fun invoke(
        noteId: String,
        generate: List<String>,
        maxAttempts: Int = 60,
        delayMillis: Long = 2000L,
    ): Flow<RegenerateResult> = flow {
        emit(RegenerateResult.Loading)

        // 1) Trigger regenerate trên backend (POST /notes/{note_id}/regenerate)
        try {
            noteRemoteRepository.regenerate(noteId, generate)
        } catch (e: HttpException) {
            emit(RegenerateResult.Error("Lỗi gọi regenerate: HTTP ${e.code()}"))
            return@flow
        } catch (e: Exception) {
            emit(RegenerateResult.Error("Lỗi gọi regenerate: ${e.message ?: "unknown"}"))
            return@flow
        }

        // 2) Poll NoteServices (qua NoteRemoteRepository.fetchNote) cho đến khi status != processing
        repeat(maxAttempts) {
            try {
                val domain: NoteDomain? = noteRemoteRepository.fetchNote(noteId)
                if (domain == null) {
                    emit(RegenerateResult.Error("Note không tồn tại trên server (null)"))
                    return@flow
                }

                when (domain.status) {
                    NoteStatus.READY -> {
                        emit(RegenerateResult.Success(domain))
                        return@flow
                    }
                    NoteStatus.ERROR -> {
                        emit(RegenerateResult.Error("Enrichment thất bại với trạng thái: ERROR"))
                        return@flow
                    }
                    else -> { /* CREATED / PROCESSING: tiếp tục poll */ }
                }
            } catch (e: HttpException) {
                if (e.code() == 404) {
                    emit(RegenerateResult.Error("Note không tồn tại trên server (404)"))
                    return@flow
                } else {
                    emit(RegenerateResult.Error("Lỗi server (${e.code()}) khi poll note"))
                    return@flow
                }
            } catch (e: Exception) {
                emit(RegenerateResult.Error("Lỗi khi poll note: ${e.message ?: "unknown"}"))
                return@flow
            }

            delay(delayMillis)
        }

        emit(RegenerateResult.Error("Timeout while waiting for enrichment"))
    }
}

sealed class RegenerateResult {
    object Loading : RegenerateResult()
    data class Success(val note: NoteDomain) : RegenerateResult()
    data class Error(val message: String) : RegenerateResult()
}
