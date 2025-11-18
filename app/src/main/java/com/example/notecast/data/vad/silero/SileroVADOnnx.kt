package com.example.notecast.data.vad.silero

import android.content.Context
import ai.onnxruntime.*
import com.example.notecast.domain.vad.VADDetector
import java.io.File
import java.io.FileOutputStream
import kotlin.collections.getOrNull
import kotlin.io.copyTo
import kotlin.io.use
import kotlin.jvm.java
import kotlin.to

/**
 * Silero ONNX VAD using ONNX Runtime Android.
 * - frameSize: expected ShortArray length (e.g., 512)
 * - threshold: probability threshold for speech
 * - minSpeechMs: minimum speech duration in ms to consider as speech (default 30ms)
 * - frameDurationMs: duration of each frame in ms (default 32ms)
 *
 * This class loads the model from assets/models/silero_vad.onnx into a temp file and creates OrtSession.
 * It accumulates speech frames to ensure minimum speech duration.
 */
class SileroVADOnnx(
    context: Context,
    private val frameSize: Int = 512,
    private val threshold: Float = 0.5f,
    private val minSpeechMs: Int = 30,
    private val frameDurationMs: Int = 32
) : VADDetector {

    private val env: OrtEnvironment = OrtEnvironment.getEnvironment()
    private val session: OrtSession

    // names (adjust if your model uses other input/output names)
    private val inputName: String
    private val outputName: String

    // Tính số frames tối thiểu cho speech liên tục
    private val minSpeechFrames = minSpeechMs / frameDurationMs

    // Counter cho speech frames liên tiếp
    private var speechCounter = 0

    init {
        // Copy model from assets to a file (onnxruntime needs file path)
        val modelFile = copyModelToFile(context, "models/silero_vad.onnx")
        val opts = OrtSession.SessionOptions()
        // enable CPU execution provider (default)
        session = env.createSession(modelFile.absolutePath, opts)

        // discover input/output names (take first)
        val inputs = session.inputNames.iterator()
        inputName = if (inputs.hasNext()) inputs.next() else throw kotlin.RuntimeException("No input in ONNX model")
        val outputs = session.outputNames.iterator()
        outputName = if (outputs.hasNext()) outputs.next() else throw kotlin.RuntimeException("No output in ONNX model")
    }

    override fun isSpeech(pcm: ShortArray): Boolean {
        if (pcm.size != frameSize) return false
        // convert short -> float normalized [-1,1]
        val floats = FloatArray(frameSize)
        var i = 0
        while (i < frameSize) {
            floats[i] = pcm[i] / 32768.0f
            i++
        }
        // create tensor: many Silero models accept shape [1, frameSize]
        val shape = longArrayOf(1, frameSize.toLong())
        val createTensorMethod = OnnxTensor::class.java.getMethod("createTensor", OrtEnvironment::class.java, Any::class.java, LongArray::class.java)
        val tensor = createTensorMethod.invoke(null, env, floats, shape) as OnnxTensor
        val input = mapOf(inputName to tensor)
        val res = session.run(input)
        // assume single scalar output or single-element array
        val out = res[0].value
        val prob: Float = when (out) {
            is FloatArray -> out.getOrNull(0) ?: 0f
            is DoubleArray -> (out.getOrNull(0) ?: 0.0).toFloat()
            is Float -> out
            is Double -> (out as Double).toFloat()
            else -> {
                // try to handle nested arrays
                try {
                    val arr = out as Array<*>
                    (arr.getOrNull(0) as? Float) ?: 0f
                } catch (e: Exception) { 0f }
            }
        }
        // cleanup
        tensor.close()
        res.close()

        // Cập nhật counter dựa trên prob
        if (prob >= threshold) {
            speechCounter++
        } else {
            speechCounter = 0
        }

        // Trả về true nếu speech liên tục đủ lâu
        return speechCounter >= minSpeechFrames
    }

    private fun copyModelToFile(context: Context, assetPath: String): File {
        val f = File(context.cacheDir, "silero_vad.onnx")
        if (f.exists()) return f
        context.assets.open(assetPath).use { input ->
            FileOutputStream(f).use { out ->
                input.copyTo(out)
            }
        }
        return f
    }

    fun close() {
        try { session.close() } catch (_: Exception) {}
    }
}
