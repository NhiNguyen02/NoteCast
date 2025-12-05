package com.example.notecast.core.asr

import ai.onnxruntime.OnnxJavaType
import ai.onnxruntime.OnnxTensor
import ai.onnxruntime.OrtEnvironment
import ai.onnxruntime.OrtSession
import ai.onnxruntime.OnnxValue
import ai.onnxruntime.TensorInfo
import android.util.Log
import java.nio.FloatBuffer
import java.nio.LongBuffer

private const val TAG_DECODER = "PhoWhisperDecoderONNX"

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
        val debugSteps: Int = 3,
    )

    /**
     * Tạo initial past_kv: 24 tensors với shape [1, 8, 0, 64] (6 layers × 4 tensors).
     * Những tensor này là Float32 arrays có length = 0.
     */
    private fun createInitialPastKv(env: OrtEnvironment): Map<String, OnnxTensor> {
        val kv = mutableMapOf<String, OnnxTensor>()
        val shape = longArrayOf(1, 8, 0, 64) // batch=1, heads=8, seq=0, head_dim=64

        repeat(6) { layer ->
            val keyDec = "past_key_values.$layer.decoder.key"
            val valDec = "past_key_values.$layer.decoder.value"
            val keyEnc = "past_key_values.$layer.encoder.key"
            val valEnc = "past_key_values.$layer.encoder.value"

            // Tạo empty tensor (0-length) đúng chuẩn float32
            val emptyBuffer = FloatBuffer.allocate(0)
            kv[keyDec] = OnnxTensor.createTensor(env, emptyBuffer, shape)
            kv[valDec] = OnnxTensor.createTensor(env, emptyBuffer, shape)
            kv[keyEnc] = OnnxTensor.createTensor(env, emptyBuffer, shape)
            kv[valEnc] = OnnxTensor.createTensor(env, emptyBuffer, shape)
        }

        return kv
    }

    /**
     * Helper: tạo use_cache_branch tensor phù hợp dtype runtime.
     * Kiểm tra session.inputInfo["use_cache_branch"] để quyết định BOOL / FLOAT / INT64.
     */
    private fun createUseCacheTensor(
        env: OrtEnvironment,
        session: OrtSession,
        value: Boolean
    ): OnnxTensor {
        val info = session.inputInfo["use_cache_branch"]
            ?: error("use_cache_branch not found in decoderSession.inputInfo")

        val tensorInfo = info.info as TensorInfo
        val shape = tensorInfo.shape ?: longArrayOf()

        // Nếu model khai báo shape rỗng (scalar) thì không truyền shape, ngược lại dùng đúng shape từ TensorInfo.
        return when (tensorInfo.type) {
            OnnxJavaType.BOOL -> {
                val arr = booleanArrayOf(value)
                // OnnxTensor only has (env, BooleanArray) overload, no shape argument
                OnnxTensor.createTensor(env, arr)
            }
            OnnxJavaType.FLOAT -> {
                val arr = floatArrayOf(if (value) 1f else 0f)
                val buf = FloatBuffer.wrap(arr)
                if (shape.isEmpty()) {
                    OnnxTensor.createTensor(env, buf)
                } else {
                    OnnxTensor.createTensor(env, buf, shape)
                }
            }
            OnnxJavaType.INT64 -> {
                val arr = longArrayOf(if (value) 1L else 0L)
                val buf = LongBuffer.wrap(arr)
                if (shape.isEmpty()) {
                    OnnxTensor.createTensor(env, buf)
                } else {
                    OnnxTensor.createTensor(env, buf, shape)
                }
            }
            else -> {
                // Fallback: treat as float scalar/vector 0/1
                val arr = floatArrayOf(if (value) 1f else 0f)
                val buf = FloatBuffer.wrap(arr)
                if (shape.isEmpty()) {
                    OnnxTensor.createTensor(env, buf)
                } else {
                    OnnxTensor.createTensor(env, buf, shape)
                }
            }
        }
    }


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

        val useCacheTensor = createUseCacheTensor(
            env = env,
            session = onnxManager.decoderSession,
            value = useCacheBranch
        )

        val inputs = mutableMapOf<String, OnnxTensor>(
            "input_ids" to inputIdsTensor,
            "encoder_hidden_states" to encoderTensor,
            "use_cache_branch" to useCacheTensor,
        )

        pastKv?.forEach { (name, tensor) ->
            inputs[name] = tensor
        }
        return inputs
    }

    /**
     * Clone OnnxTensor data into a NEW OnnxTensor that lives beyond OrtSession.Result.
     * NOTE: this function handles common primitive flat array cases (FloatArray, LongArray, BooleanArray).
     * If runtime returns nested multi-dimensional typed arrays, you may need to extend this logic.
     */
    private fun cloneTensor(env: OrtEnvironment, t: OnnxTensor): OnnxTensor {
        val info = t.info as TensorInfo
        val shape = info.shape ?: longArrayOf()

        fun hasNonPositiveDim(s: LongArray): Boolean = s.any { it <= 0L }

        val value = t.value

        // Nếu tensor rỗng hoặc có shape chứa dimension <= 0, tránh tạo tensor mới
        // vì ORT sẽ ném OrtException. Với KV cache, reuse luôn tensor gốc là an toàn
        // trong phạm vi vòng lặp hiện tại.
        if ((value is FloatArray && value.isEmpty()) ||
            (value is LongArray && value.isEmpty()) ||
            (value is BooleanArray && value.isEmpty()) ||
            (shape.isNotEmpty() && hasNonPositiveDim(shape))
        ) {
//            return t
            return OnnxTensor.createTensor(env, FloatBuffer.allocate(0), shape)
        }

        return when (info.type) {
            OnnxJavaType.FLOAT -> {
                when (val raw = value) {
                    is FloatArray -> {
                        if (hasNonPositiveDim(shape)) {
                            OnnxTensor.createTensor(env, raw)
                        } else {
                            OnnxTensor.createTensor(env, FloatBuffer.wrap(raw.clone()), shape)
                        }
                    }
                    else -> OnnxTensor.createTensor(env, raw)
                }
            }
            OnnxJavaType.INT64 -> {
                when (val raw = value) {
                    is LongArray -> {
                        if (hasNonPositiveDim(shape)) {
                            OnnxTensor.createTensor(env, raw)
                        } else {
                            OnnxTensor.createTensor(env, LongBuffer.wrap(raw.clone()), shape)
                        }
                    }
                    else -> OnnxTensor.createTensor(env, raw)
                }
            }
            OnnxJavaType.BOOL -> {
                when (val raw = value) {
                    is BooleanArray -> OnnxTensor.createTensor(env, raw.clone())
                    else -> OnnxTensor.createTensor(env, raw)
                }
            }
            else -> OnnxTensor.createTensor(env, value)
        }
    }

    /**
     * Cập nhật KV cache từ OrtSession.Result.
     * Lọc các output có tên bắt đầu bằng "present." (present.N.decoder/encoder.key/value)
     * và gom lại thành map<String, OnnxTensor> để dùng cho bước decode sau.
     */
    private fun updatePastKvFromOutputs(
        env: OrtEnvironment,
        ortResult: OrtSession.Result,
        previousKv: Map<String, OnnxTensor>?,
    ): Map<String, OnnxTensor> {
        // Giải phóng cache cũ để tránh rò rỉ bộ nhớ
        previousKv?.values?.forEach { it.close() }

        val newKv = mutableMapOf<String, OnnxTensor>()
        for ((name, value) in ortResult) {
            if (!name.startsWith("present.")) continue
            val tensor = value as? OnnxTensor ?: continue
            val mappedName = name.replaceFirst("present.", "past_key_values.")
            val cloned = cloneTensor(env, tensor)
            newKv[mappedName] = cloned
        }
        return newKv
    }

    private fun getLogitsValue(outputs: OrtSession.Result): OnnxValue {
        for ((name, value) in outputs) {
            if (name == "logits") {
                return value as OnnxValue
            }
        }
        return outputs[0]
    }

    fun runAutoregressive(
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

        var pastKv: Map<String, OnnxTensor>? = createInitialPastKv(env)

        Log.d(TAG_DECODER, "runAutoregressive: tEnc=$tEnc, initialToken=${tokens.first()}")

        try {
            repeat(config.maxTokens) { step ->
                val currentTokenId = tokens.last()

                val inputs = buildInputs(
                    env = env,
                    encoderTensor = encoderTensor,
                    currentTokenId = currentTokenId,
                    pastKv = pastKv,
                    useCacheBranch = step > 0
                )

                onnxManager.decoderSession.run(inputs).use { outputs ->
                    try {
                        val logitsOnnxValue: OnnxValue = getLogitsValue(outputs)
//                        @Suppress("UNCHECKED_CAST")
//                        val logits = logitsOnnxValue.value as Array<Array<FloatArray>>
//                        val lastLogits = logits[0][0]
                        val raw = logitsOnnxValue.value
                        val lastLogits: FloatArray = when (raw) {
                            is Array<*> -> {
                                when (raw[0]) {
                                    is Array<*> -> (raw[0] as Array<FloatArray>)[0]
                                    is FloatArray -> raw[0] as FloatArray
                                    else -> error("Unsupported logits type: ${raw::class}")
                                }
                            }
                            is FloatArray -> raw
                            is FloatBuffer -> {
                                FloatArray(raw.remaining()).also { raw.get(it) }
                            }
                            else -> error("Unsupported logits type: ${raw::class}")
                        }

                        val nextId = argmax(lastLogits).toLong()

                        if (step < config.debugSteps) {
                            val maxLogit = lastLogits.maxOrNull() ?: Float.NaN
                            Log.d(TAG_DECODER, "step=$step currentToken=$currentTokenId nextId=$nextId maxLogit=$maxLogit")
                        }

                        tokens += nextId

                        if (nextId == config.eosTokenId) {
                            Log.d(TAG_DECODER, "EOS reached at step=$step, totalTokens=${tokens.size}")
//                            inputs.values.forEach { it.close() }
                            inputs.forEach { (name, tensor) ->
                                // KHÔNG đóng encoder_hidden_states
                                if (name == "encoder_hidden_states") return@forEach

                                // KHÔNG đóng past_key_values.*, vì nó được clone và lưu lại (sẽ đóng ở updatePastKv)
                                if (name.startsWith("past_key_values")) return@forEach

                                tensor.close()
                            }

                            return tokenizer.decode(tokens.toLongArray())
                        }

                        pastKv = updatePastKvFromOutputs(env, outputs, pastKv)
                    } finally {
//                        inputs.values.forEach { it.close() }
                        inputs.forEach { (name, tensor) ->
                            // KHÔNG đóng encoder_hidden_states
                            if (name == "encoder_hidden_states") return@forEach

                            // KHÔNG đóng past_key_values.*, vì nó được clone và lưu lại (sẽ đóng ở updatePastKv)
                            if (name.startsWith("past_key_values")) return@forEach

                            tensor.close()
                        }

                    }
                }
            }
        } finally {
            encoderTensor.close()
            pastKv?.values?.forEach { it.close() }
        }

        Log.d(TAG_DECODER, "maxTokens reached, totalTokens=${tokens.size}")
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
