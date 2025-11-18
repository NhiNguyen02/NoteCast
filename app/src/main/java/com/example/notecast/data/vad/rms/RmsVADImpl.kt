package com.example.notecast.data.vad.rms

import com.example.notecast.domain.vad.VADDetector
import kotlin.collections.isEmpty
import kotlin.math.log10
import kotlin.math.sqrt

/**
 * RMS-based VAD.
 * - frameSize: samples per frame (e.g., 320 for 20ms @16k)
 * - thresholdDb: threshold in dBFS to consider speech
 * - minSpeechMs: minimal continuous ms to mark speech (internal logic should handle)
 */
class RmsVADImpl(
    val frameSize: Int = 320,
    private val thresholdDb: Double = -40.0,
    private val minSpeechMs: Int = 50,
    private val frameDurationMs: Int = 20
) : VADDetector {

    private var accumulatedSpeechMs = 0

    override fun isSpeech(pcm: ShortArray): Boolean {
        if (pcm.size != frameSize) return false
        val rms = computeRMS(pcm)
        val db = rmsToDb(rms)
        val active = db >= thresholdDb
        if (active) {
            accumulatedSpeechMs += frameDurationMs
            if (accumulatedSpeechMs >= minSpeechMs) return true
        } else {
            accumulatedSpeechMs = 0
        }
        return false
    }

    private fun computeRMS(frame: ShortArray): Double {
        if (frame.isEmpty()) return 0.0
        var sum = 0.0
        for (s in frame) {
            val v = s.toDouble() / Short.MAX_VALUE
            sum += v * v
        }
        val mean = sum / frame.size
        return sqrt(mean)
    }

    private fun rmsToDb(rms: Double): Double {
        if (rms <= 0.0) return -120.0
        return 20.0 * log10(rms)
    }
}
