package com.example.notecast.core.asr

import ai.onnxruntime.OnnxTensor

/**
 * Minimal abstraction for pho whisper backends so we can provide a no-op fallback when ONNX session
 * creation fails on devices without required kernels.
 */
interface PhoWhisperEngine {
    suspend fun runEncoder(melSpectrogram: Array<FloatArray>): OnnxTensor
    suspend fun runDecoderGreedy(encoderOutputs: OnnxTensor): IntArray
    fun decodeTokens(tokenIds: IntArray): String
    fun decodeVerbose(tokenIds: IntArray): Pair<String, List<String>>
    fun isAvailable(): Boolean
}
