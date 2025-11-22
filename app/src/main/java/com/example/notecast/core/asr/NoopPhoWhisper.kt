package com.example.notecast.core.asr

import ai.onnxruntime.OnnxTensor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import android.util.Log

/**
 * A fallback implementation that returns empty/placeholder outputs and avoids ONNX usage.
 */
class NoopPhoWhisper : PhoWhisperEngine {
    private val TAG = "NoopPhoWhisper"

    override suspend fun runEncoder(melSpectrogram: Array<FloatArray>): OnnxTensor = withContext(Dispatchers.Default) {
        Log.w(TAG, "runEncoder called on NoopPhoWhisper -> unsupported")
        throw UnsupportedOperationException("Noop engine does not provide encoder outputs")
    }

    override suspend fun runDecoderGreedy(encoderOutputs: OnnxTensor): IntArray = withContext(Dispatchers.Default) {
        Log.w(TAG, "runDecoderGreedy called on NoopPhoWhisper -> returning empty tokens")
        intArrayOf()
    }

    override fun decodeTokens(tokenIds: IntArray): String {
        Log.w(TAG, "decodeTokens called on NoopPhoWhisper -> returning empty string")
        return ""
    }

    override fun decodeVerbose(tokenIds: IntArray): Pair<String, List<String>> {
        Log.w(TAG, "decodeVerbose called on NoopPhoWhisper -> returning empty structures")
        return "" to emptyList()
    }

    override fun isAvailable(): Boolean = false
}
