package com.example.notecast.domain.usecase.audio

import com.example.notecast.domain.repository.VADRepository
import java.io.File
import javax.inject.Inject


/**
 * Ghi âm -> chạy trim im lặng (dùng VADRepository) -> trả lại file đã trim.
 */
class TrimSilenceUseCase @Inject constructor(
    private val vadRepository: VADRepository,
) {
    /**
     * @param inputWavPath path tới WAV gốc.
     * @return File WAV đã trim hoặc file gốc nếu không xử lý được.
     */
    operator fun invoke(inputWavPath: String): File {
        val inputFile = File(inputWavPath)
        return vadRepository.trimSilenceOffline(inputFile)
    }
}