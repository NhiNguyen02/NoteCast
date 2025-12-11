package com.example.notecast.domain.repository

import java.io.File

/**
 * Xử lý tiền xử lý audio ở tầng domain (trim im lặng, v.v.).
 * Implementation sẽ sống ở data/core.
 */
interface VADRepository {
    /**
     * Trim im lặng đầu/cuối và im lặng dài giữa câu (>= threshold) trên file WAV 16kHz mono PCM16.
     *
     * @param inputWavFile file WAV gốc.
     * @return file WAV mới đã trim hoặc file gốc nếu không thể xử lý.
     */
    fun trimSilenceOffline(inputWavFile: File): File
}