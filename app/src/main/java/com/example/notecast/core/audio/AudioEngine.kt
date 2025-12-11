package com.example.notecast.core.audio

import android.Manifest
import android.content.Context
import androidx.annotation.RequiresPermission
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.io.RandomAccessFile
import javax.inject.Inject
import javax.inject.Singleton

/**
 * AudioEngine chịu trách nhiệm:
 * - Khởi tạo AudioRecord 16 kHz, mono, PCM_16BIT thông qua AudioRecorder
 * - Đọc audio liên tục trên background thread
 * - Đẩy PCM frames tới 3 buffer: VAD, ASR, Recorder
 *
 * Engine này không biết gì về VAD/ASR cụ thể, chỉ publish PCM.
 */
@Singleton
class AudioEngine @Inject constructor(
    private val recorder: AudioRecorder,
    private val vadBuffer: AudioBuffer<ShortArray>,
    private val asrBuffer: AudioBuffer<ShortArray>,
    private val recorderBuffer: AudioBuffer<ShortArray>,
    private val appContext: Context,
) {

    private val scope = CoroutineScope(Dispatchers.IO)
    private var recordingJob: Job? = null
    private var currentCallback: ((ShortArray) -> Unit)? = null

    // Expose fixed config to downstream (ASR pipeline contract)
    val sampleRate: Int get() = 16_000
    val channels: Int get() = 1

    // File path for current recording session (WAV 16kHz mono PCM16)
    @Volatile
    var currentRecordingFilePath: String? = null
        private set

    private var pcmFile: File? = null
    private var wavFile: File? = null
    private var pcmOut: FileOutputStream? = null

    @RequiresPermission(Manifest.permission.RECORD_AUDIO)
    @Synchronized
    fun start(onPcmFrame: ((ShortArray) -> Unit)? = null) {
        if (recordingJob?.isActive == true) return

        if (onPcmFrame != null) {
            currentCallback = onPcmFrame
        }

        // clear buffers between sessions
        (vadBuffer as? ClearableBuffer)?.clear()
        (asrBuffer as? ClearableBuffer)?.clear()
        (recorderBuffer as? ClearableBuffer)?.clear()

        // prepare PCM temp file
        preparePcmFile()

        recordingJob = scope.launch {
            recorder.start { frame ->
                val copy = frame.copyOf()
                vadBuffer.write(copy)
                asrBuffer.write(copy)
                recorderBuffer.write(copy)

                // append raw PCM16 to file
                writePcmFrame(copy)

                currentCallback?.invoke(copy)
            }
        }
    }

    private fun preparePcmFile() {
        closePcmFile()
        val dir = File(appContext.cacheDir, "recordings").apply { mkdirs() }
        pcmFile = File.createTempFile("rec_", ".pcm", dir)
        pcmOut = FileOutputStream(pcmFile)
        wavFile = null
        currentRecordingFilePath = null
    }

    private fun writePcmFrame(frame: ShortArray) {
        try {
            val out = pcmOut ?: return
            val buffer = ByteArray(frame.size * 2)
            var idx = 0
            frame.forEach { s ->
                val v = s.toInt()
                buffer[idx++] = (v and 0xFF).toByte()
                buffer[idx++] = ((v ushr 8) and 0xFF).toByte()
            }
            out.write(buffer)
        } catch (_: Throwable) {
        }
    }

    private fun closePcmFile() {
        try {
            pcmOut?.flush()
            pcmOut?.close()
        } catch (_: Throwable) {
        }
        pcmOut = null
    }

    suspend fun stop() {
        recorder.stop()
        recordingJob?.cancelAndJoin()
        recordingJob = null

        // finalize PCM file -> WAV and set currentRecordingFilePath
        closePcmFile()
        finalizeWavFile()

        (vadBuffer as? ClearableBuffer)?.clear()
        (asrBuffer as? ClearableBuffer)?.clear()
        (recorderBuffer as? ClearableBuffer)?.clear()

        currentCallback = null
    }

    fun pause() {
        recorder.stop()
        recordingJob?.cancel()
        recordingJob = null
        // keep PCM file open; resume will append more data
    }

    @RequiresPermission(Manifest.permission.RECORD_AUDIO)
    fun resume() {
        if (recordingJob?.isActive == true) return

        recordingJob = scope.launch {
            recorder.start { frame ->
                val copy = frame.copyOf()
                vadBuffer.write(copy)
                asrBuffer.write(copy)
                recorderBuffer.write(copy)
                writePcmFrame(copy)
                currentCallback?.invoke(copy)
            }
        }
    }

    private fun finalizeWavFile() {
        val pcm = pcmFile ?: return
        if (!pcm.exists()) return

        try {
            val dir = pcm.parentFile ?: appContext.cacheDir
            val wav = File(dir, pcm.nameWithoutExtension + ".wav")
            writeWavFromPcm(pcm, wav, sampleRate, channels)
            currentRecordingFilePath = wav.absolutePath
            wavFile = wav
        } catch (_: Throwable) {
            currentRecordingFilePath = null
        } finally {
            // delete raw pcm to save space
            try { pcm.delete() } catch (_: Throwable) {}
            pcmFile = null
        }
    }

    private fun writeWavFromPcm(pcmFile: File, wavFile: File, sampleRate: Int, channels: Int) {
        val totalAudioLen = pcmFile.length()
        val totalDataLen = totalAudioLen + 36
        val byteRate = 16 * sampleRate * channels / 8

        val input = pcmFile.inputStream()
        val out = RandomAccessFile(wavFile, "rw")
        out.setLength(0)

        // RIFF header
        val header = ByteArray(44)
        header[0] = 'R'.code.toByte()
        header[1] = 'I'.code.toByte()
        header[2] = 'F'.code.toByte()
        header[3] = 'F'.code.toByte()

        writeIntLE(header, 4, totalDataLen.toInt())

        header[8] = 'W'.code.toByte()
        header[9] = 'A'.code.toByte()
        header[10] = 'V'.code.toByte()
        header[11] = 'E'.code.toByte()

        header[12] = 'f'.code.toByte()
        header[13] = 'm'.code.toByte()
        header[14] = 't'.code.toByte()
        header[15] = ' '.code.toByte()

        writeIntLE(header, 16, 16) // Subchunk1Size for PCM
        writeShortLE(header, 20, 1) // AudioFormat PCM
        writeShortLE(header, 22, channels.toShort())
        writeIntLE(header, 24, sampleRate)
        writeIntLE(header, 28, byteRate)
        writeShortLE(header, 32, (channels * 16 / 8).toShort()) // block align
        writeShortLE(header, 34, 16) // bits per sample

        header[36] = 'd'.code.toByte()
        header[37] = 'a'.code.toByte()
        header[38] = 't'.code.toByte()
        header[39] = 'a'.code.toByte()

        writeIntLE(header, 40, totalAudioLen.toInt())

        out.write(header)

        val buffer = ByteArray(4096)
        while (true) {
            val read = input.read(buffer)
            if (read <= 0) break
            out.write(buffer, 0, read)
        }

        input.close()
        out.close()
    }

    private fun writeIntLE(buf: ByteArray, offset: Int, value: Int) {
        buf[offset] = (value and 0xFF).toByte()
        buf[offset + 1] = ((value ushr 8) and 0xFF).toByte()
        buf[offset + 2] = ((value ushr 16) and 0xFF).toByte()
        buf[offset + 3] = ((value ushr 24) and 0xFF).toByte()
    }

    private fun writeShortLE(buf: ByteArray, offset: Int, value: Short) {
        val v = value.toInt()
        buf[offset] = (v and 0xFF).toByte()
        buf[offset + 1] = ((v ushr 8) and 0xFF).toByte()
    }
}
