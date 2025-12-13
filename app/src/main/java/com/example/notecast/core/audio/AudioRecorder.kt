package com.example.notecast.core.audio

import android.Manifest
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.util.Log
import androidx.annotation.RequiresPermission

/**
 * Wrapper đơn giản quanh AudioRecord để tách Android API khỏi AudioEngine.
 *
 * IMPORTANT: This recorder is designed to ALWAYS run at 16 kHz, mono, PCM_16BIT.
 * - sampleRate = 16_000 Hz
 * - channelConfig = CHANNEL_IN_MONO
 * - audioFormat = ENCODING_PCM_16BIT
 * Changing these may break VAD/ASR pipeline expectations (MelProcessor, hopLength/winLength, etc.).
 */
private const val TAG_RECORDER = "AudioRecorder"

class AudioRecorder(
    private val sampleRate: Int = 16_000,
    private val channelConfig: Int = AudioFormat.CHANNEL_IN_MONO,
    private val audioFormat: Int = AudioFormat.ENCODING_PCM_16BIT,
) {
    @Volatile
    var isRecording: Boolean = false
        private set

    private var audioRecord: AudioRecord? = null

    @RequiresPermission(Manifest.permission.RECORD_AUDIO)
    private fun createAudioRecord(): AudioRecord {
        // Defensive check: enforce 16kHz mono PCM_16BIT
        require(sampleRate == 16_000) { "AudioRecorder must use 16 kHz sample rate (got $sampleRate)" }
        require(channelConfig == AudioFormat.CHANNEL_IN_MONO) { "AudioRecorder must be mono input (got $channelConfig)" }
        require(audioFormat == AudioFormat.ENCODING_PCM_16BIT) { "AudioRecorder must use PCM_16BIT (got $audioFormat)" }

        val minBuffer = AudioRecord.getMinBufferSize(sampleRate, channelConfig, audioFormat)
        Log.d(TAG_RECORDER, "createAudioRecord: sr=$sampleRate, ch=$channelConfig, fmt=$audioFormat, minBuffer=$minBuffer")
        if (minBuffer == AudioRecord.ERROR || minBuffer == AudioRecord.ERROR_BAD_VALUE) {
            Log.e(TAG_RECORDER, "createAudioRecord: getMinBufferSize error=$minBuffer")
        }

        val bufferSizeInBytes = minBuffer.coerceAtLeast(sampleRate / 10 * 2) // ~100ms
        val record = AudioRecord(
            MediaRecorder.AudioSource.MIC,
            sampleRate,
            channelConfig,
            audioFormat,
            bufferSizeInBytes
        )
        Log.d(TAG_RECORDER, "createAudioRecord: state=${record.state}")
        return record
    }

    /**
     * Bắt đầu đọc audio và callback từng frame PCM16.
     * Frame size = 20ms @16kHz = 320 samples (mặc định phù hợp WebRTC VAD).
     */
    @RequiresPermission(Manifest.permission.RECORD_AUDIO)
    fun start(
        frameSize: Int = 320,
        onFrame: (ShortArray) -> Unit,
    ) {
        if (isRecording) return
        val record = createAudioRecord()
        audioRecord = record
        Log.d(TAG_RECORDER, "start: state=${record.state}")
        if (record.state != AudioRecord.STATE_INITIALIZED) {
            Log.e(TAG_RECORDER, "start: AudioRecord NOT initialized, state=${record.state}")
            return
        }
        val buffer = ShortArray(frameSize)
        Log.d(TAG_RECORDER, "start: recordingState=${record.recordingState}")
        record.startRecording()
        isRecording = true

        try {
            while (isRecording) {
                val read = record.read(buffer, 0, buffer.size)
                if (read <= 0) {
                    Log.w(TAG_RECORDER, "read() returned $read")
                    continue
                }
                val frame = if (read == buffer.size) buffer.copyOf() else buffer.copyOf(read)
                onFrame(frame)
            }
        } finally {
            try {
                record.stop()
            } catch (_: Throwable) {
            }
            record.release()
            audioRecord = null
            isRecording = false
        }
    }

    fun stop() {
        isRecording = false
    }
}
