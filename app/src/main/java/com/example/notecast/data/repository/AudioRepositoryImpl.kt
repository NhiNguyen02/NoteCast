package com.example.notecast.data.repository

import android.Manifest
import androidx.annotation.RequiresPermission
import com.example.notecast.core.audio.AudioEngine
import com.example.notecast.core.audio.FrameBufferManager
import com.example.notecast.core.vad.VADManager
import com.example.notecast.domain.repository.AudioRepository
import com.example.notecast.domain.vad.VadState
import com.example.notecast.presentation.ui.record.RecordingState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import javax.inject.Inject
import kotlin.collections.copyOf
import kotlin.collections.copyOfRange
import kotlin.collections.filter
import kotlin.collections.isEmpty
import kotlin.collections.sumOf
import kotlin.collections.withIndex
import kotlin.math.*
import kotlin.ranges.coerceIn
import kotlin.ranges.until

class AudioRepositoryImpl @Inject constructor(
    private val audioEngine: AudioEngine,
    private val vadManager: VADManager
) : AudioRepository {

    private val _recordingState = MutableStateFlow(RecordingState.Idle)
    override val recordingState: Flow<RecordingState> = _recordingState.asStateFlow()

    private val _amplitude = MutableStateFlow(0f)
    override val amplitude: Flow<Float> = _amplitude.asStateFlow()

    override val vadState: Flow<VadState> = vadManager.state

    private val _lastPcmFrame = MutableStateFlow<ShortArray?>(null)
    override val lastPcmFrame: Flow<ShortArray?> = _lastPcmFrame.asStateFlow()

    private val _waveform = MutableStateFlow<List<Float>>(emptyList())
    override val waveform: Flow<List<Float>> = _waveform.asStateFlow()

    private val _bufferAvailableSamples = MutableStateFlow(0)
    override val bufferAvailableSamples: Flow<Int> = _bufferAvailableSamples.asStateFlow()

    // internal buffer to produce fixed-size frames for trimming/export
    private val frameBuffer = FrameBufferManager(initialCapacity = 16000)

    private val recordedFrames = kotlin.collections.ArrayList<ShortArray>()
    private val recordedChunks = kotlin.collections.ArrayList<ShortArray>()

    init {
        // audio callback
        audioEngine.pcmCallback = { chunk ->
            // copy to avoid external mutation
            val copy = chunk.copyOf()

            // 1) push into internal frameBuffer
            frameBuffer.push(copy)

            // 2) while we have full frames, pop them and pass to VAD + recordedFrames
            val expected = try { vadManager.getExpectedFrameSize() } catch (_: Throwable) { 512 }
            while (true) {
                val frame = frameBuffer.popFrame(expected) ?: break
                try {
                    vadManager.processFrame(frame) // update VAD state internally
                } catch (_: Throwable) { /* ignore VAD errors */ }
                recordedFrames.add(frame)
            }

            // 3) update UI flows (amplitude / waveform / lastFrame / buffer)
            _amplitude.value = computeAmplitude(copy)
            _waveform.value = downsampleToFloats(copy, 200)
            _lastPcmFrame.value = copy
            _bufferAvailableSamples.value = frameBuffer.availableSamples()

            // 4) save raw chunk for raw export if needed
            recordedChunks.add(copy)
        }
    }

    @RequiresPermission(Manifest.permission.RECORD_AUDIO)
    override fun startRecording() {
        // reset buffers for a fresh session
        recordedChunks.clear()
        recordedFrames.clear()
        // ensure frameBuffer cleared
        // easiest: create a new instance or provide clear API; we'll reuse existing flushAll
        frameBuffer.flushAll()
        try {
            audioEngine.start()
            _recordingState.value = RecordingState.Recording
        } catch (e: Exception) {
            e.printStackTrace()
            _recordingState.value = RecordingState.Idle
        }
    }

    override fun pauseRecording() {
        audioEngine.pause()
        _recordingState.value = RecordingState.Paused
    }

    override fun resumeRecording() {
        audioEngine.resume()
        _recordingState.value = RecordingState.Recording
    }

    override fun stopRecording() {
        // enter stopping state
        _recordingState.value = RecordingState.Stopping

        // stop engine
        audioEngine.stop()

        // flush remaining samples from frameBuffer into frames
        val remaining = frameBuffer.flushAll()
        val expected = try { vadManager.getExpectedFrameSize() } catch (_: Throwable) { 512 }

        // split remaining into full frames and tail
        var idx = 0
        while (idx + expected <= remaining.size) {
            val f = remaining.copyOfRange(idx, idx + expected)
            // also run through vadManager to maintain consistency (stateless)
            try { vadManager.processFrame(f) } catch (_: Throwable) {}
            recordedFrames.add(f)
            idx += expected
        }

        // if leftover tail samples exist, keep as raw chunk
        val tailSize = remaining.size - idx
        if (tailSize > 0) {
            val tail = remaining.copyOfRange(idx, remaining.size)
            recordedChunks.add(tail)
        }

        // final state Idle
        _recordingState.value = RecordingState.Idle
    }

    override fun isRecording(): Boolean {
        return audioEngine.isRecording()
    }

    override fun getBufferAvailableSamples(): Int = frameBuffer.availableSamples()

    override suspend fun getTrimmedRecording(prePaddingChunks: Int, postPaddingChunks: Int): ShortArray {
        return withContext(Dispatchers.Default) {
            if (recordedFrames.isEmpty()) return@withContext ShortArray(0)

            val n = recordedFrames.size
            val keep = BooleanArray(n)
            for (i in 0 until n) {
                keep[i] = try {
                    vadManager.processFrameCheck(recordedFrames[i])
                } catch (_: Throwable) {
                    false
                }
            }

            // apply padding
            for (i in 0 until n) {
                if (keep[i]) {
                    val s = max(0, i - prePaddingChunks)
                    val e = min(n - 1, i + postPaddingChunks)
                    for (k in s..e) keep[k] = true
                }
            }

            val total = recordedFrames.withIndex().filter { keep[it.index] }.sumOf { it.value.size }
            val out = ShortArray(total)
            var pos = 0
            for (i in 0 until n) {
                if (!keep[i]) continue
                val f = recordedFrames[i]
                System.arraycopy(f, 0, out, pos, f.size)
                pos += f.size
            }
            out
        }
    }

    private fun computeAmplitude(frame: ShortArray): Float {
        var maxv = 0
        for (s in frame) {
            val v = abs(s.toInt())
            if (v > maxv) maxv = v
        }
        return (maxv.toFloat() / Short.MAX_VALUE).coerceIn(0f, 1f)
    }

    private fun downsampleToFloats(frame: ShortArray, points: Int): List<Float> {
        if (frame.isEmpty()) return emptyList()
        val step = max(1, frame.size / points)
        val result = kotlin.collections.ArrayList<Float>(min(points, frame.size))
        var i = 0
        while (i < frame.size) {
            var maxv = 0
            val end = min(frame.size, i + step)
            for (j in i until end) {
                val v = abs(frame[j].toInt())
                if (v > maxv) maxv = v
            }
            result.add(maxv.toFloat() / Short.MAX_VALUE.toFloat())
            i += step
        }
        return result
    }

}
