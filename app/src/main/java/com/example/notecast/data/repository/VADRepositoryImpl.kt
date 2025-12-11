package com.example.notecast.data.repository

import android.util.Log
import com.example.notecast.core.vad.VADManager
import com.example.notecast.data.vad.rms.RmsVADImpl
import com.example.notecast.data.vad.silero.SileroVADOnnx
import com.example.notecast.data.vad.webrtc.WebRtcVADImpl
import com.example.notecast.domain.repository.VADRepository
import java.io.File
import java.io.RandomAccessFile
import javax.inject.Inject

private const val TAG_TRIM = "TrimSilenceRepo"

class VADRepositoryImpl @Inject constructor (
    private val vadManager: VADManager,
) : VADRepository {
    private val sampleRate: Int = 16_000
    private val minSilenceGapSeconds: Double = 5.0

    data class SpeechSegment(val startSample: Int, val endSampleExclusive: Int)

    override fun trimSilenceOffline(inputWavFile: File): File {
        if (!inputWavFile.exists()) return inputWavFile

        Log.d(TAG_TRIM, "trimSilenceOffline: input=${inputWavFile.absolutePath}")

        val pcmSamples = try {
            readPcmFromWav(inputWavFile)
        } catch (t: Throwable) {
            Log.e(TAG_TRIM, "readPcmFromWav error: ${t.message}", t)
            return inputWavFile
        }
        if (pcmSamples.isEmpty()) {
            Log.w(TAG_TRIM, "trimSilenceOffline: empty PCM, return original")
            return inputWavFile
        }

        val originalDurationSec = pcmSamples.size.toDouble() / sampleRate

        val frameSize = when (val det = vadManager.detector) {
            is WebRtcVADImpl -> det.frameSize
            is RmsVADImpl -> det.frameSize
            is SileroVADOnnx -> det.frameSize
            else -> 320
        }

        val rawSegments = findSpeechSegments(pcmSamples, frameSize)
        if (rawSegments.isEmpty()) {
            Log.w(TAG_TRIM, "trimSilenceOffline: no segments, durationSec=$originalDurationSec")
            return inputWavFile
        }

        Log.d(TAG_TRIM, "trimSilenceOffline: originalSec=$originalDurationSec, rawSegments=${rawSegments.size}")

        val mergedSegments = mergeSegmentsWithSilenceThreshold(rawSegments)
        val speechSamplesTotal = mergedSegments.sumOf { (it.endSampleExclusive - it.startSample).toLong() }.toInt()
        val speechDurationSec = speechSamplesTotal.toDouble() / sampleRate

        Log.d(TAG_TRIM, "trimSilenceOffline: mergedSegments=${mergedSegments.size}, speechSec=$speechDurationSec")

        // Single segment => giống trim head/tail cũ
        if (mergedSegments.size == 1) {
            val seg = mergedSegments.first()
            val trimmedLength = seg.endSampleExclusive - seg.startSample
            val trimmed = ShortArray(trimmedLength)
            System.arraycopy(pcmSamples, seg.startSample, trimmed, 0, trimmedLength)
            val cleanFile = File(
                inputWavFile.parentFile ?: inputWavFile.parentFile,
                inputWavFile.nameWithoutExtension + "_clean.wav"
            )
            writeWavFromPcm(trimmed, cleanFile, sampleRate, channels = 1)
            Log.d(TAG_TRIM, "trimSilenceOffline: single seg, outSec=${trimmedLength.toDouble() / sampleRate}")
            return cleanFile
        }

        // Nhiều đoạn => ghép lại, loại bỏ im lặng >= 5s
        val joined = ShortArray(speechSamplesTotal)
        var dst = 0
        for (seg in mergedSegments) {
            val len = seg.endSampleExclusive - seg.startSample
            System.arraycopy(pcmSamples, seg.startSample, joined, dst, len)
            dst += len
        }

        val cleanFile = File(
            inputWavFile.parentFile ?: inputWavFile.parentFile,
            inputWavFile.nameWithoutExtension + "_clean.wav"
        )
        writeWavFromPcm(joined, cleanFile, sampleRate, channels = 1)
        Log.d(TAG_TRIM, "trimSilenceOffline: joined segs=${mergedSegments.size}, outSec=$speechDurationSec")
        return cleanFile
    }

    private fun findSpeechSegments(samples: ShortArray, frameSize: Int): List<SpeechSegment> {
        val detector = vadManager.detector
        val segments = mutableListOf<SpeechSegment>()

        var inSpeech = false
        var segStartFrame = 0
        var frameIndex = 0
        var offset = 0

        while (offset + frameSize <= samples.size) {
            val frame = ShortArray(frameSize)
            System.arraycopy(samples, offset, frame, 0, frameSize)
            val isSpeech = detector.isSpeech(frame)

            if (isSpeech && !inSpeech) {
                inSpeech = true
                segStartFrame = frameIndex
            } else if (!isSpeech && inSpeech) {
                inSpeech = false
                val segEndFrame = frameIndex
                val startSample = segStartFrame * frameSize
                val endSampleExclusive = segEndFrame * frameSize
                segments += SpeechSegment(startSample, endSampleExclusive)
            }

            frameIndex++
            offset += frameSize
        }

        if (inSpeech) {
            val startSample = segStartFrame * frameSize
            val endSampleExclusive = samples.size
            segments += SpeechSegment(startSample, endSampleExclusive)
        }

        return segments
    }

    private fun mergeSegmentsWithSilenceThreshold(
        segments: List<SpeechSegment>
    ): List<SpeechSegment> {
        if (segments.isEmpty()) return emptyList()
        if (segments.size == 1) return segments

        val merged = mutableListOf<SpeechSegment>()
        var current = segments.first()

        for (i in 1 until segments.size) {
            val next = segments[i]
            val gapSamples = next.startSample - current.endSampleExclusive
            val gapSeconds = gapSamples.toDouble() / sampleRate

            if (gapSeconds < minSilenceGapSeconds) {
                current = SpeechSegment(
                    startSample = current.startSample,
                    endSampleExclusive = next.endSampleExclusive
                )
            } else {
                merged += current
                current = next
            }
        }

        merged += current
        return merged
    }

    // readPcmFromWav, writeWavFromPcm, writeIntLE, writeShortLE:
    // (di chuyển nguyên xi từ TrimSilenceUseCase sang đây)
    private fun readPcmFromWav(wavFile: File): ShortArray {
        val raf = RandomAccessFile(wavFile, "r")
        val totalLen = raf.length().toInt()
        if (totalLen <= 44) {
            raf.close()
            return ShortArray(0)
        }
        // skip 44-byte header, assume standard PCM WAV
        raf.seek(44)
        val pcmLenBytes = totalLen - 44
        val bytes = ByteArray(pcmLenBytes)
        raf.readFully(bytes)
        raf.close()

        val samples = ShortArray(pcmLenBytes / 2)
        var i = 0
        var j = 0
        while (i + 1 < bytes.size) {
            val lo = bytes[i].toInt() and 0xFF
            val hi = bytes[i + 1].toInt() shl 8
            samples[j] = (hi or lo).toShort()
            i += 2
            j += 1
        }
        return samples
    }
    private fun writeWavFromPcm(pcm: ShortArray, wavFile: File, sampleRate: Int, channels: Int) {
        val totalAudioLen = pcm.size * 2L
        val totalDataLen = totalAudioLen + 36
        val byteRate = 16 * sampleRate * channels / 8

        val out = RandomAccessFile(wavFile, "rw")
        out.setLength(0)

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
        var byteIdx = 0
        var bufIdx: Int
        while (byteIdx < pcm.size) {
            bufIdx = 0
            while (bufIdx + 1 < buffer.size && byteIdx < pcm.size) {
                val v = pcm[byteIdx].toInt()
                buffer[bufIdx++] = (v and 0xFF).toByte()
                buffer[bufIdx++] = ((v ushr 8) and 0xFF).toByte()
                byteIdx++
            }
            out.write(buffer, 0, bufIdx)
        }

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