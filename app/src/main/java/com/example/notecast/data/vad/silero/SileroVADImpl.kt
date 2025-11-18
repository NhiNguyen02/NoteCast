package com.example.notecast.data.vad.silero

import com.example.notecast.domain.vad.VADDetector
import android.content.Context
import ai.onnxruntime.*
import java.nio.FloatBuffer
import kotlin.collections.indices
import kotlin.io.readBytes
import kotlin.to

/**
 * Silero VAD (ONNX) placeholder.
 * - frameSize default 512 (approx 32ms @16k)
 * - minSpeechMs default 30
 *
 * Real implementation should use ONNX Runtime:
 *  - add dependency com.microsoft.onnxruntime:onnxruntime-android
 *  - load model from assets and run inference
 *
 * For now this class demonstrates expected API and fallback behavior.
 */

class SileroVADImpl(
    context: Context,
    modelPath: String,
    private val frameSize: Int,
    private val minSpeechMs: Int,
    private val frameDurationMs: Int,
) : VADDetector {

    private val env = OrtEnvironment.getEnvironment()
    private val session: OrtSession

    private var speechAccumMs = 0

    init {
        val bytes = context.assets.open(modelPath).readBytes()
        session = env.createSession(bytes)
    }

    override fun isSpeech(frame: ShortArray): Boolean {
        val input = FloatArray(frameSize)

        // PCM16 → float32 normalized
        for (i in frame.indices) {
            input[i] = frame[i] / 32768f
        }

        val tensor = OnnxTensor.createTensor(env, FloatBuffer.wrap(input),
            longArrayOf(1, frameSize.toLong())
        )

        val output = session.run(mapOf("input" to tensor))
        val prob = (output[0].value as Array<FloatArray>)[0][0]

        val isSpeech = prob > 0.5f

        if (isSpeech) {
            speechAccumMs += frameDurationMs
            return speechAccumMs >= minSpeechMs
        } else {
            speechAccumMs = 0
            return false
        }
    }
}
