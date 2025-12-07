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
import kotlin.math.exp
import kotlin.random.Random

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
        val temperature: Float = 1.0f,
        val topK: Int = 0,
        val topP: Float = 0.0f,
        val seed: Long? = null,
    )

    /**
     * Helper: tạo use_cache_branch tensor phù hợp dtype runtime.
     * Kiểm tra session.inputInfo["use_cache_branch"] để quyết định BOOL / FLOAT / INT64.
     */
    /**
     * use_cache_branch: model yêu cầu boolean[1]
     * -> luôn tạo BooleanArray(1) tương ứng, không còn nhánh FLOAT / INT64.
     */
    private fun createUseCacheTensor(
        env: OrtEnvironment,
        session: OrtSession,
        value: Boolean
    ): OnnxTensor {
        val info = session.inputInfo["use_cache_branch"]
            ?: error("use_cache_branch not found in decoderSession.inputInfo")

        val tensorInfo = info.info as TensorInfo
        require(tensorInfo.type == OnnxJavaType.BOOL) {
            "Expected use_cache_branch to be BOOL, but got ${tensorInfo.type}"
        }

        // Model: use_cache_branch: boolean[1]
        val arr = booleanArrayOf(value)
        return OnnxTensor.createTensor(env, arr)
    }


    /**
     * Helper build input cho một bước decode.
     * BIND: input_ids, encoder_hidden_states, use_cache_branch, và TOÀN BỘ past_key_values.N.*
     */
    private fun buildInputs(
        env: OrtEnvironment,
        encoderTensor: OnnxTensor,
        currentTokenId: Long,
        pastKv: Map<String, OnnxTensor>?,
    ): MutableMap<String, OnnxTensor> {
        val inputIds = LongArray(1) { currentTokenId }
        val inputIdsTensor = OnnxTensor.createTensor(
            env,
            LongBuffer.wrap(inputIds),
            longArrayOf(1, 1)
        )

        val useCacheBranch = pastKv != null && pastKv.isNotEmpty()
        val useCacheTensor = createUseCacheTensor(
            env = env,
            session = onnxManager.decoderSession,
            value = useCacheBranch
        )

        val inputs = mutableMapOf(
            "input_ids" to inputIdsTensor,
            "encoder_hidden_states" to encoderTensor,
            "use_cache_branch" to useCacheTensor,
        )

        // FEED LẠI TOÀN BỘ KV: cả encoder lẫn decoder
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
//        val shape = info.shape ?: longArrayOf()
        val value = t.value

        val rawShape = info.shape ?: longArrayOf()
        val shape = if (rawShape.isNotEmpty()) {
            rawShape.map { dim -> if (dim <= 0L) 1L else dim }.toLongArray()
        } else {
            longArrayOf()
        }

        // KHÔNG throw nữa, chỉ optional log nếu có dim<=0
        if (rawShape.any { it <= 0L }) {
            Log.w(TAG_DECODER, "cloneTensor: dynamic dims in $rawShape, using $shape instead")
        }

        return when (info.type) {
            OnnxJavaType.FLOAT -> {
                when (val raw = value) {
                    is FloatArray -> {
                        if (shape.isEmpty()) {
                            OnnxTensor.createTensor(env, raw.clone())
                        } else {
                            OnnxTensor.createTensor(env, FloatBuffer.wrap(raw.clone()), shape)
                        }
                    }
                    is Array<*> -> {
                        val totalSize = shape.fold(1L) { acc, dim -> acc * dim }
                        val flattened = flattenNestedArrayToFloat(raw, totalSize.toInt())
                        OnnxTensor.createTensor(env, FloatBuffer.wrap(flattened), shape)
                    }
                    is FloatBuffer -> {
                        val totalSize = if (shape.isNotEmpty()) {
                            shape.fold(1L) { acc, dim -> acc * dim }.toInt()
                        } else raw.remaining()
                        val arr = FloatArray(totalSize)
                        raw.duplicate().get(arr)
                        if (shape.isEmpty()) {
                            OnnxTensor.createTensor(env, arr)
                        } else {
                            OnnxTensor.createTensor(env, FloatBuffer.wrap(arr), shape)
                        }
                    }
                    else -> OnnxTensor.createTensor(env, raw)
                }
            }
            OnnxJavaType.INT64 -> {
                when (val raw = value) {
                    is LongArray -> {
                        if (shape.isEmpty()) {
                            OnnxTensor.createTensor(env, raw.clone())
                        } else {
                            OnnxTensor.createTensor(env, LongBuffer.wrap(raw.clone()), shape)
                        }
                    }
                    is Array<*> -> {
                        val totalSize = shape.fold(1L) { acc, dim -> acc * dim }
                        val flattened = flattenNestedArrayToLong(raw, totalSize.toInt())
                        OnnxTensor.createTensor(env, LongBuffer.wrap(flattened), shape)
                    }
                    is LongBuffer -> {
                        val totalSize = if (shape.isNotEmpty()) {
                            shape.fold(1L) { acc, dim -> acc * dim }.toInt()
                        } else raw.remaining()
                        val arr = LongArray(totalSize)
                        raw.duplicate().get(arr)
                        if (shape.isEmpty()) {
                            OnnxTensor.createTensor(env, arr)
                        } else {
                            OnnxTensor.createTensor(env, LongBuffer.wrap(arr), shape)
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

    private fun flattenNestedArrayToFloat(arr: Array<*>, expectedSize: Int): FloatArray {
        val result = FloatArray(expectedSize)
        var idx = 0
        fun recurse(obj: Any?) {
            when (obj) {
                is FloatArray -> obj.forEach { if (idx < expectedSize) result[idx++] = it }
                is Array<*> -> obj.forEach { recurse(it) }
                is Float -> if (idx < expectedSize) result[idx++] = obj
            }
        }
        recurse(arr)
        if (idx != expectedSize) {
            Log.w(
                TAG_DECODER,
                "flattenNestedArrayToFloat: filled=$idx, expected=$expectedSize (possible mismatch with declared shape)"
            )
        }
        return result
    }

    private fun flattenNestedArrayToLong(arr: Array<*>, expectedSize: Int): LongArray {
        val result = LongArray(expectedSize)
        var idx = 0
        fun recurse(obj: Any?) {
            when (obj) {
                is LongArray -> obj.forEach { if (idx < expectedSize) result[idx++] = it }
                is Array<*> -> obj.forEach { recurse(it) }
                is Long -> if (idx < expectedSize) result[idx++] = obj
            }
        }
        recurse(arr)
        if (idx != expectedSize) {
            Log.w(
                TAG_DECODER,
                "flattenNestedArrayToLong: filled=$idx, expected=$expectedSize (possible mismatch with declared shape)"
            )
        }
        return result
    }

    /**
     * Cập nhật KV cache từ OrtSession.Result.
     * Lấy tất cả output "present.N.decoder/encoder.key/value"
     * -> map sang "past_key_values.N.decoder/encoder.key/value".
     */
    private fun updatePastKvFromOutputs(
        env: OrtEnvironment,
        ortResult: OrtSession.Result,
        previousKv: Map<String, OnnxTensor>?,
    ): Map<String, OnnxTensor> {
        // Giải phóng cache cũ
        previousKv?.values?.forEach { it.close() }

        val newKv = mutableMapOf<String, OnnxTensor>()

        for ((name, value) in ortResult) {
            if (!name.startsWith("present.")) continue

            val tensor = value as? OnnxTensor ?: continue
            val info = tensor.info as TensorInfo
            val rawShape = info.shape ?: longArrayOf()
            // 0 hoặc -1 coi như dynamic, với batch_size=1 thì thay bằng 1
            val effectiveShape = if (rawShape.isNotEmpty()) {
                rawShape.map { dim -> if (dim <= 0L) 1L else dim }.toLongArray()
            } else {
                longArrayOf() // scalar / để cloneTensor tự xử
            }

            // Map present.* -> past_key_values.*
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

    /**
     * Softmax over logits with temperature.
     */
    private fun softmax(logits: FloatArray, temperature: Float): FloatArray {
        val t = if (temperature <= 0f) 1e-6f else temperature
        var maxLogit = Float.NEGATIVE_INFINITY
        for (v in logits) if (v / t > maxLogit) maxLogit = v / t
        var sum = 0.0
        val probs = FloatArray(logits.size)
        for (i in logits.indices) {
            val v = logits[i] / t
            val e = exp((v - maxLogit).toDouble()).toFloat()
            probs[i] = e
            sum += e
        }
        val inv = (1.0 / sum).toFloat()
        for (i in probs.indices) probs[i] *= inv
        return probs
    }

    /**
     * Sample next token from logits using temperature + topK/topP.
     * If all sampling disabled (topK=0, topP=0, temperature=1), falls back to greedy argmax.
     */
    private fun sampleNextToken(
        logits: FloatArray,
        config: DecoderConfig,
    ): Long {
        val greedy = (config.temperature == 1.0f && config.topK <= 0 && config.topP <= 0f)
        if (greedy) {
            return argmax(logits).toLong()
        }
        val probs = softmax(logits, config.temperature)

        // Apply topK
        val indices = probs.indices.toList()
        val sorted = indices.sortedByDescending { probs[it] }
        val filteredByTopK = if (config.topK > 0 && config.topK < sorted.size) {
            sorted.take(config.topK)
        } else sorted

        // Apply topP (nucleus)
        var cum = 0.0f
        val filteredByTopP = if (config.topP > 0f && config.topP < 1f) {
            val list = mutableListOf<Int>()
            for (idx in filteredByTopK) {
                val p = probs[idx]
                cum += p
                list += idx
                if (cum >= config.topP) break
            }
            if (list.isEmpty()) filteredByTopK else list
        } else filteredByTopK

        // Renormalize
        var sumP = 0.0f
        for (i in filteredByTopP) sumP += probs[i]
        val renorm = if (sumP > 0f) 1f / sumP else 1f

        val rng = if (config.seed != null) Random(config.seed) else Random.Default
        val r = rng.nextFloat()
        var acc = 0.0f
        for (i in filteredByTopP) {
            acc += probs[i] * renorm
            if (r <= acc) return i.toLong()
        }
        return filteredByTopP.first().toLong()
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

        // Bước 1: không có KV cache, để model chạy nhánh no-cache theo IO spec
        var pastKv: Map<String, OnnxTensor>? = null

        Log.d(TAG_DECODER, "runAutoregressive: tEnc=$tEnc, initialToken=${tokens.first()}")

        try {
            repeat(config.maxTokens) { step ->
                val currentTokenId = tokens.last()

                val inputs = buildInputs(
                    env = env,
                    encoderTensor = encoderTensor,
                    currentTokenId = currentTokenId,
                    pastKv = pastKv,
                )

                onnxManager.decoderSession.run(inputs).use { outputs ->
                    try {
                        val logitsOnnxValue: OnnxValue = getLogitsValue(outputs)
                        val raw = logitsOnnxValue.value
                        val lastLogits: FloatArray = when (raw) {
                            is Array<*> -> {
                                val first = raw.getOrNull(0)
                                when (first) {
                                    is Array<*> -> {
                                        val inner = first.getOrNull(0)
                                        when (inner) {
                                            is FloatArray -> inner
                                            is FloatBuffer -> FloatArray(inner.remaining()).also { inner.get(it) }
                                            else -> error("Unsupported nested logits type: ${inner?.let { it::class.toString() } ?: "null"}")
                                        }
                                    }
                                    is FloatArray -> first
                                    is FloatBuffer -> FloatArray(first.remaining()).also { first.get(it) }
                                    else -> error("Unsupported logits type: ${first?.let { it::class.toString() } ?: "null"}")
                                }
                            }
                            is FloatArray -> raw
                            is FloatBuffer -> {
                                FloatArray(raw.remaining()).also { raw.get(it) }
                            }
                            else -> error("Unsupported logits type: ${raw::class}")
                        }

                        val nextId = sampleNextToken(lastLogits, config)

                        if (step < config.debugSteps) {
                            val maxLogit = lastLogits.maxOrNull() ?: Float.NaN
                            Log.d(TAG_DECODER, "step=$step currentToken=$currentTokenId nextId=$nextId maxLogit=$maxLogit")
                        }

                        tokens += nextId

                        if (nextId == config.eosTokenId) {
                            Log.d(TAG_DECODER, "EOS reached at step=$step, totalTokens=${tokens.size}")
                            inputs.forEach { (name, tensor) ->
                                if (name == "encoder_hidden_states") return@forEach
                                if (name.startsWith("past_key_values")) return@forEach
                                tensor.close()
                            }
                            return tokenizer.decode(tokens.toLongArray())
                        }

                        pastKv = updatePastKvFromOutputs(env, outputs, pastKv)
                    } finally {
                        inputs.forEach { (name, tensor) ->
                            if (name == "encoder_hidden_states") return@forEach
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
