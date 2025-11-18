package com.example.notecast.data.vad.webrtc

import com.example.notecast.domain.vad.VADDetector
import kotlin.collections.isEmpty
import kotlin.math.log10
import kotlin.math.sqrt

/**
 * Placeholder wrapper for WebRTC VAD. Replace internal call with actual WebRTC bindings.
 *
 * Frame size default 320 (20ms @16k).
 * sensitivity 0..3 (3 most sensitive)
 */
class WebRtcVADImpl(
    val frameSize: Int = 320,
    private val sensitivity: Int = 3,
    private val minSpeechMs: Int = 50,
    private val frameDurationMs: Int = 20
) : VADDetector {

    private var speechAccum = 0

    override fun isSpeech(pcm: ShortArray): Boolean {
        if (pcm.size != frameSize) return false
        // TODO: replace with actual WebRTC VAD call:
        // val active = webRtcVad.isSpeech(pcm, sampleRate=16000)
        // For now use RMS heuristic but with more sensitivity
        val rms = computeRMS(pcm)
        val thresholdDb = -42.0 + (3 - sensitivity) * 2.0  // tuned heuristic
        val active = rmsToDb(rms) >= thresholdDb
        if (active) {
            speechAccum += frameDurationMs
            if (speechAccum >= minSpeechMs) return true
        } else {
            speechAccum = 0
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
