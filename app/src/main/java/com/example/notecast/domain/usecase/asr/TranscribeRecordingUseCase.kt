package com.example.notecast.domain.usecase.asr

import com.example.notecast.domain.model.AsrResult
import com.example.notecast.domain.repository.AsrRepository
import com.example.notecast.domain.service.RemoteStorageUploader
import com.example.notecast.domain.usecase.audio.TrimSilenceUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject

/**
 * TranscribeRecordingUseCase
 *
 * Orchestration cho luồng:
 *  Ghi âm -> trim im lặng đầu/cuối -> upload Firebase Storage -> lấy downloadUrl -> gọi backend PhoWhisper -> trả về AsrResult.
 */
class TranscribeRecordingUseCase @Inject constructor(
    private val remoteStorageUploader: RemoteStorageUploader,
    private val asrRepository: AsrRepository,
    private val trimSilenceUseCase: TrimSilenceUseCase,
) {

    /**
     * @param localAudioFile File audio local (ví dụ .wav hoặc .m4a) sau khi ghi âm xong.
     * @return [AsrResult] chứa transcript đầy đủ và danh sách chunks.
     */
    suspend operator fun invoke(localAudioFile: File): AsrResult = withContext(Dispatchers.IO){
        // 1) Chạy offline VAD để cắt bỏ im lặng đầu/cuối (nếu có)
        val fileForUpload = trimSilenceUseCase(localAudioFile.absolutePath)

        // 2) Upload lên Firebase Storage để lấy download URL
        val downloadUrl = remoteStorageUploader.uploadAndGetUrl(fileForUpload)

        // 3) Gọi backend PhoWhisper qua HF Space với audio_url = downloadUrl
        asrRepository.transcribeByUrl(downloadUrl)
    }
}
