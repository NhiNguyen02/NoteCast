package com.example.notecast.domain.usecase.audio

import com.example.notecast.data.remote.PhoWhisperApi
import com.example.notecast.data.remote.TranscribeUrlRequest
import com.example.notecast.domain.model.AudioDomain
import com.example.notecast.domain.model.NoteDomain
import com.example.notecast.domain.model.NoteStatus
import com.example.notecast.domain.model.NoteType
import com.example.notecast.domain.repository.NoteRepository
import com.example.notecast.domain.service.RemoteStorageUploader
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject

/**
 * Use case: sau khi ghi âm xong, upload audio lên Cloudinary, gọi PhoWhisper /transcribe-url,
 * lưu NoteDomain(status=PROCESSING) vào Room và trả về noteId.
 */
class TranscribeRecordingUseCase @Inject constructor(
    private val remoteStorageUploader: RemoteStorageUploader,
    private val phoWhisperApi: PhoWhisperApi,
    private val noteRepository: NoteRepository,
) {
    suspend operator fun invoke(localFilePath: String, userId: String? = null): String = withContext(Dispatchers.IO) {
            // 1) Upload audio lên Cloudinary (thông qua RemoteStorageUploader) để lấy URL công khai
        val audioUrl = remoteStorageUploader.uploadAndGetUrl(File(localFilePath))

        // 2) Gọi PhoWhisper /transcribe-url
        val resp = phoWhisperApi.transcribeUrl(
            TranscribeUrlRequest(audio_url = audioUrl, user_id = userId)
        )

        val noteId = resp.note_id
        val durationSec = resp.duration ?: 0.0

        // 3) Tạo NoteDomain tối thiểu trạng thái PROCESSING
        val now = System.currentTimeMillis()
        val note = NoteDomain(
            id = noteId,
            type = NoteType.AUDIO,
            title = null,
            rawText = null,
            normalizedText = null,
            keywords = emptyList(),
            summary = null,
            mindmapJson = null,
            audio = AudioDomain(
                durationSec = durationSec,
                sampleRate = 16000,
                chunks = emptyList(),
                localFilePath = localFilePath,
                cloudUrl = audioUrl,
            ),
            folderId = null,
            status = NoteStatus.PROCESSING,
            createdAt = now,
            updatedAt = now,
        )

        // 4) Lưu NoteDomain vào Room qua NoteRepository
        noteRepository.saveNote(note)

        noteId
    }
}
