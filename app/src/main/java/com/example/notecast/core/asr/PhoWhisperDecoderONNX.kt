package com.example.notecast.core.asr

import ai.onnxruntime.OnnxTensor
import ai.onnxruntime.OrtEnvironment
import ai.onnxruntime.OrtSession
import ai.onnxruntime.OnnxValue
import java.nio.FloatBuffer
import java.nio.LongBuffer

/**
 * PhoWhisperDecoderONNX
 * - Chạy vòng lặp decoder autoregressive với past_key_values.
 * - Tham chiếu IO spec: docs/onnx-model-io-signatures-2025-12-03.md
 *   (xác nhận tên input: "input_ids", "encoder_hidden_states", "past_key_values.*", "use_cache_branch").
 */
class PhoWhisperDecoderONNX(
    private val onnxManager: ONNXRuntimeManager,
    private val tokenizer: Tokenizer,
) {
    data class DecoderConfig(
        val maxTokens: Int = 128,
        val eosTokenId: Long = 50257L,
        val padTokenId: Long = 50256L,
    )

    /**
     * Helper xây map input cho một bước decode.
     * Bind đầy đủ input_ids, encoder_hidden_states, past_key_values.N.* và use_cache_branch theo IO spec.
     */
    private fun buildInputs(
        env: OrtEnvironment,
        encoderTensor: OnnxTensor,
        currentTokenId: Long,
        pastKv: Map<String, OnnxTensor>?,
        useCacheBranch: Boolean,
    ): MutableMap<String, OnnxTensor> {
        val inputIds = LongArray(1) { currentTokenId }
        val inputIdsTensor = OnnxTensor.createTensor(
            env,
            LongBuffer.wrap(inputIds),
            longArrayOf(1, 1)
        )

        // use_cache_branch là scalar BOOL, dùng overload createTensor(env, value)
        val useCacheTensor = OnnxTensor.createTensor(env, useCacheBranch)

        val inputs = mutableMapOf<String, OnnxTensor>(
            "input_ids" to inputIdsTensor,
            "encoder_hidden_states" to encoderTensor,
            "use_cache_branch" to useCacheTensor,
        )
        pastKv?.forEach { (name, tensor) -> inputs[name] = tensor }
        return inputs
    }

    /**
     * Cập nhật KV cache từ OrtSession.Result.
     * Lọc các output có tên bắt đầu bằng "present." (present.N.decoder/encoder.key/value)
     * và gom lại thành map<String, OnnxTensor> để dùng cho bước decode sau.
     */
    private fun updatePastKvFromOutputs(
        ortResult: OrtSession.Result,
        previousKv: Map<String, OnnxTensor>?,
    ): Map<String, OnnxTensor> {
        // Giải phóng cache cũ để tránh rò rỉ bộ nhớ
        previousKv?.values?.forEach { it.close() }

        val newKv = mutableMapOf<String, OnnxTensor>()
        for ((name, value) in ortResult) {
            if (!name.startsWith("present.")) continue
            // Dùng chính tên output làm key để bind trở lại vào input past_key_values.* ở bước tiếp theo
            val tensor = value as? OnnxTensor ?: continue
            newKv[name] = tensor
        }
        return newKv
    }

    suspend fun runAutoregressive(
        encoderOut: PhoWhisperEncoderONNX.EncoderOutput,
        config: DecoderConfig = DecoderConfig()
    ): String {
        val env = OrtEnvironment.getEnvironment()
        val hidden = encoderOut.hiddenStates
        val tEnc = encoderOut.length
        val encShape = longArrayOf(1, tEnc.toLong(), 512L)
        val encoderTensor = OnnxTensor.createTensor(env, FloatBuffer.wrap(hidden), encShape)

        val tokens = mutableListOf<Long>()
        tokens += tokenizer.bosTokenId.toLong()

        var pastKv: Map<String, OnnxTensor>? = null

        try {
            repeat(config.maxTokens) {
                val currentTokenId = tokens.last()

                val inputs = buildInputs(
                    env = env,
                    encoderTensor = encoderTensor,
                    currentTokenId = currentTokenId,
                    pastKv = pastKv,
                    useCacheBranch = pastKv != null,
                )

                val outputs: OrtSession.Result = onnxManager.decoderSession.run(inputs)

                try {
                    // logits luôn tồn tại, nên lấy trực tiếp từ outputs theo tên hoặc index
                    val logitsRaw = outputs.get("logits") ?: outputs[0]
                    val logitsOnnxValue = logitsRaw as OnnxValue
                    @Suppress("UNCHECKED_CAST")
                    val logits = logitsOnnxValue.value as Array<Array<FloatArray>>
                    val lastLogits = logits[0][0]
                    val nextId = argmax(lastLogits).toLong()
                    tokens += nextId

                    if (nextId == config.eosTokenId) {
                        return tokenizer.decode(tokens.toLongArray())
                    }

                    // Cập nhật KV cache cho bước decode tiếp theo
                    pastKv = updatePastKvFromOutputs(outputs, pastKv)
                } finally {
                    // Giải phóng các input tensor tạo trong buildInputs (encoderTensor được tái sử dụng)
                    inputs.values.forEach { it.close() }
                }
            }
        } finally {
            encoderTensor.close()
            pastKv?.values?.forEach { it.close() }
        }

        return tokenizer.decode(tokens.toLongArray())
    }

    private fun argmax(arr: FloatArray): Int {
        var maxIdx = 0
        var maxVal = Float.NEGATIVE_INFINITY
        for (i in arr.indices) {
            val v = arr[i]
            if (v > maxVal) {
                maxVal = v
                maxIdx = i
            }
        }
        return maxIdx
    }
}
