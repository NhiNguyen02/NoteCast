package com.example.notecast.domain.usecase

import com.example.notecast.domain.repository.RecorderRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.RandomAccessFile
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

class TrimAndExportWavUseCase @Inject constructor(
    private val repository: RecorderRepository
) {
    suspend operator fun invoke(
        prePadding: Int,
        postPadding: Int,
        outputDir: File
    ): ProcessResult {
        return withContext(Dispatchers.IO) {
            try {
                //Lấy dữ liệu PCM đã cắt gọt từ Repository
                val pcmData = repository.getTrimmedRecording(prePadding, postPadding)
                if (pcmData.isEmpty()) {
                    return@withContext ProcessResult(null, 0, "No audio data")
                }

                //Tạo file output
                if (!outputDir.exists()) outputDir.mkdirs()
                val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
                val outFile = File(outputDir, "REC_$timestamp.wav")

                // Ghi Header WAV và Data
                writeWavFile(outFile, pcmData)

                // Tính thời lượng
                val durationMs = (pcmData.size.toLong() * 1000) / 16000 // Sample rate 16kHz

                ProcessResult(outFile, durationMs)
            } catch (e: Exception) {
                e.printStackTrace()
                ProcessResult(null, 0, e.message)
            }
        }
    }

    private fun writeWavFile(file: File, data: ShortArray) {
        val sampleRate = 16000
        val channels = 1
        val byteRate = sampleRate * channels * 2 // 16-bit
        val totalDataLen = data.size * 2
        val totalChunkSize = 36 + totalDataLen

        val header = ByteBuffer.allocate(44)
        header.order(ByteOrder.LITTLE_ENDIAN)

        // RIFF/WAVE header
        header.put("RIFF".toByteArray())
        header.putInt(totalChunkSize)
        header.put("WAVE".toByteArray())
        header.put("fmt ".toByteArray())
        header.putInt(16) // Subchunk1Size (16 for PCM)
        header.putShort(1.toShort()) // AudioFormat (1 = PCM)
        header.putShort(channels.toShort())
        header.putInt(sampleRate)
        header.putInt(byteRate)
        header.putShort((channels * 2).toShort()) // BlockAlign
        header.putShort(16.toShort()) // BitsPerSample
        header.put("data".toByteArray())
        header.putInt(totalDataLen)

        FileOutputStream(file).use { fos ->
            fos.write(header.array())
            // Convert ShortArray to ByteArray
            val buffer = ByteBuffer.allocate(data.size * 2)
            buffer.order(ByteOrder.LITTLE_ENDIAN)
            for (s in data) {
                buffer.putShort(s)
            }
            fos.write(buffer.array())
        }
    }
}