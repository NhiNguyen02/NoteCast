package com.example.notecast.domain.repository

import kotlinx.coroutines.flow.Flow

/**
 * Interface chuyên điều khiển ghi âm mức domain/data.
 * ĐÃ loại bỏ phụ thuộc vào tầng UI (không còn dùng RecordingState UI enum).
 */
interface AudioRepository {
    /** Bắt đầu ghi âm. */
    fun startRecording()

    /** Dừng ghi âm. */
    suspend fun stopRecording()

    /** Tạm dừng ghi âm. */
    fun pauseRecording()

    /** Tiếp tục ghi âm. */
    fun resumeRecording()

    // đọc frames từ ASRBuffer (hoặc VADBuffer) dưới dạng Flow
    fun asrPcmFrames(): Flow<ShortArray>

    /**
     * Stream các frame PCM16 đã được chuẩn hóa bởi AudioEngine.
     * Dùng cho VAD/Segmenter/ASR pipeline.
     */
    fun streamPcmFrames(): Flow<ShortArray>
}