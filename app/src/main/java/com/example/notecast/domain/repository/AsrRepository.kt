package com.example.notecast.domain.repository

import com.example.notecast.domain.model.AsrResult

/**
 * AsrRepository
 *
 * Abstraction cho pipeline ASR từ URL audio (Firebase Storage, v.v.)
 * tới kết quả transcript đầy đủ + danh sách chunks.
 */
interface AsrRepository {

    /**
     * Gọi backend PhoWhisper để nhận diện giọng nói từ một URL audio.
     *
     * @param audioUrl URL public/authorized tới file audio (ví dụ downloadUrl từ Firebase Storage).
     * @return [AsrResult] chứa transcript toàn đoạn, duration, sampleRate và danh sách chunks.
     */
    suspend fun transcribeByUrl(audioUrl: String): AsrResult
}