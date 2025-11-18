package com.example.notecast.core.audio

import android.Manifest
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import androidx.annotation.RequiresPermission
import kotlinx.coroutines.*
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.max

typealias PcmCallback = (ShortArray) -> Unit

@Singleton
class AudioEngine @Inject constructor(
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.Default + SupervisorJob())
) {
    private val sampleRate = 16000
    private val channelConfig = AudioFormat.CHANNEL_IN_MONO
    private val audioFormat = AudioFormat.ENCODING_PCM_16BIT

    private var audioRecord: AudioRecord? = null
    private var readJob: Job? = null
    private var paused = false

    private val minBufferSize = AudioRecord.getMinBufferSize(sampleRate, channelConfig, audioFormat)
    private val readBufferSize = max(minBufferSize, sampleRate / 4) // ~250ms by default

    var pcmCallback: PcmCallback? = null

    @RequiresPermission(Manifest.permission.RECORD_AUDIO)
    fun start() {
        if (readJob?.isActive == true) return
        if (minBufferSize <= 0) {
            throw IllegalStateException("Invalid audio buffer size: $minBufferSize")
        }
        audioRecord = AudioRecord(
            MediaRecorder.AudioSource.MIC,
            sampleRate,
            channelConfig,
            audioFormat,
            readBufferSize * 2
        )
        if (audioRecord?.state != AudioRecord.STATE_INITIALIZED) {
            audioRecord?.release()
            audioRecord = null
            throw IllegalStateException("Failed to initialize AudioRecord")
        }
        audioRecord?.startRecording()
        paused = false
        readJob = scope.launch {
            val buffer = ShortArray(readBufferSize)
            while (isActive && audioRecord?.recordingState == AudioRecord.RECORDSTATE_RECORDING) {
                if (paused) {
                    delay(30)
                    continue
                }
                val read = audioRecord?.read(buffer, 0, buffer.size) ?: 0
                if (read > 0) {
                    val chunk = if (read == buffer.size) buffer.copyOf() else buffer.copyOf(read)
                    pcmCallback?.invoke(chunk)
                } else {
                    delay(10)
                }
            }
        }
    }

    fun stop() {
        readJob?.cancel()
        readJob = null
        try {
            audioRecord?.stop()
        } catch (_: Exception) {}
        audioRecord?.release()
        audioRecord = null
        paused = false
    }

    fun pause() {
        paused = true
    }

    fun resume() {
        paused = false
    }

    fun isRecording(): Boolean {
        return readJob?.isActive == true && audioRecord?.recordingState == AudioRecord.RECORDSTATE_RECORDING && !paused
    }
}
