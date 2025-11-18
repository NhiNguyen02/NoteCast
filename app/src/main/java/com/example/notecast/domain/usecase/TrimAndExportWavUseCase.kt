package com.example.notecast.domain.usecase

import com.example.notecast.core.audio.writeWavFile
import com.example.notecast.domain.repository.AudioRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject
import javax.inject.Named

data class ProcessResult(val file: File?, val recordedMs: Long, val message: String? = null)

class TrimAndExportWavUseCase @Inject constructor(
    private val audioRepository: AudioRepository,
    @Named("IO") private val ioDispatcher: CoroutineDispatcher
) {
    /**
     * process: trim → write wav file if non-empty
     * return ProcessResult with saved file path (or null if nothing)
     */
    suspend operator fun invoke(prePaddingChunks: Int = 1, postPaddingChunks: Int = 1, outputDir: File): ProcessResult {
        return withContext(ioDispatcher) {
            val pcm = audioRepository.getTrimmedRecording(prePaddingChunks, postPaddingChunks)
            if (pcm.isEmpty()) {
                return@withContext ProcessResult(null, 0L, "No speech detected")
            }
            val file = File(outputDir, "record_trimmed_${System.currentTimeMillis()}.wav")
            try {
                writeWavFile(file, pcm) // implement elsewhere (util)
                // approximate recordedMs by sampleCount / sampleRate
                val recordedMs = (pcm.size.toDouble() / 16000.0 * 1000.0).toLong()
                ProcessResult(file, recordedMs, null)
            } catch (t: Throwable) {
                t.printStackTrace()
                ProcessResult(null, 0L, t.message)
            }
        }
    }
}
