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

    /** Dùng cho waveform/VAD realtime (nếu còn dùng). */
    fun streamPcmFrames(): Flow<ShortArray>

    /** Audio params (fixed for ASR contract) */
    val sampleRate: Int
    val channels: Int

    /** Đường dẫn file PCM/WAV của phiên ghi âm hiện tại (sau khi stop). */
    val currentRecordingFilePath: String?
}