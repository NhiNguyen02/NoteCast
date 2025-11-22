package com.example.notecast.core.asr

import android.content.Context
import ai.onnxruntime.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.nio.FloatBuffer
import java.nio.LongBuffer
import android.util.Log
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class PhoWhisperONNX(
    private val context: Context,
    // Default to the non-quantized model folder present in assets
    encoderAssetPath: String = "phowhisper-base-onnx/encoder_model.onnx",
    decoderAssetPath: String = "phowhisper-base-onnx/decoder_model.onnx",
    private val maxDecodeSteps: Int = 300
) : PhoWhisperEngine {
    private val TAG = "PhoWhisperONNX"
    private val env: OrtEnvironment = OrtEnvironment.getEnvironment()
    private val sessionOptions = OrtSession.SessionOptions().apply {
        setInterOpNumThreads(1)
        setIntraOpNumThreads(Runtime.getRuntime().availableProcessors())
    }

    private lateinit var encoderSession: OrtSession
    private lateinit var decoderSession: OrtSession

    // Model input/output names (resolved from session after initialization)
    private var encoderInputName: String = "mel" // default fallback
    private var encoderOutputName: String = "output"
    private var decoderInputTokens: String = "tokens"
    private var decoderInputEncoder: String = "encoder_output"
    private var decoderOutputLogits: String = "logits"

    private val tokenizer = PhoWhisperTokenizer(context)

    // We'll defer heavy session initialization until first use to avoid doing this on DI / main thread.
    private val initLock = Any()
    private var sessionsInitialized = false
    private val encoderAssetPathFinal = encoderAssetPath
    private val decoderAssetPathFinal = decoderAssetPath

    // Helper: copy asset to a file in cache and return the file (reuse if already exists)
    private fun copyAssetToCache(assetPath: String): File {
        val name = assetPath.replace('/', '_')
        val outFile = File(context.cacheDir, name)
        if (outFile.exists()) return outFile
        try {
            context.assets.open(assetPath).use { input ->
                FileOutputStream(outFile).use { out ->
                    input.copyTo(out)
                }
            }
        } catch (ioe: IOException) {
            throw ioe
        }
        return outFile
    }

    // Build list of candidate paths from a template path (quantized and non-quantized variants)
    private fun buildCandidates(path: String): List<String> {
        val folder = path.substringBeforeLast('/')
        val file = path.substringAfterLast('/')
        val fileVariants = listOf(
            file,
            file.replace("_quantized", ""),
            file.replace("-quantized", "")
        ).distinct()
        val folderVariants = listOf(
            folder,
            folder.replace("-quantized", ""),
            folder.replace("_quantized", "")
        ).distinct()
        val candidates = ArrayList<String>()
        for (f in folderVariants) {
            for (fn in fileVariants) {
                candidates.add("$f/$fn")
            }
        }
        return listOf(path) + candidates.filter { it != path }
    }

    // Synchronized initializer called on-demand from background threads
    private fun ensureSessionsInitialized() {
        if (sessionsInitialized) return
        synchronized(initLock) {
            if (sessionsInitialized) return
            val encoderCandidates = buildCandidates(encoderAssetPathFinal)
            val decoderCandidates = buildCandidates(decoderAssetPathFinal)

            var initialized = false
            var lastError: Exception? = null
            for (encPath in encoderCandidates) {
                for (decPath in decoderCandidates) {
                    try {
                        Log.d(TAG, "Initializing PhoWhisperONNX with encoder=$encPath decoder=$decPath")
                        val encFile = copyAssetToCache(encPath)
                        val decFile = copyAssetToCache(decPath)
                        encoderSession = env.createSession(encFile.absolutePath, sessionOptions)
                        decoderSession = env.createSession(decFile.absolutePath, sessionOptions)
                        // Resolve actual input/output names from sessions
                        try {
                            val encInputs = encoderSession.inputInfo.keys
                            if (encInputs.isNotEmpty()) {
                                encoderInputName = encInputs.first()
                            }
                            val encOutputs = encoderSession.outputInfo.keys
                            if (encOutputs.isNotEmpty()) {
                                encoderOutputName = encOutputs.first()
                            }
                        } catch (ex: Exception) {
                            Log.w(TAG, "Unable to resolve encoder IO names: ${ex.message}")
                        }
                        try {
                            val decInputs = decoderSession.inputInfo.keys
                            // heuristic for decoder token input
                            decoderInputTokens = decInputs.find { it.contains("token", true) || it.contains("input_id", true) || it.contains("ids", true) } ?: decInputs.first()
                            // heuristic for decoder encoder input
                            decoderInputEncoder = decInputs.find { it.contains("encoder", true) || it.contains("encoder_output", true) } ?: decInputs.find { it != decoderInputTokens } ?: decInputs.first()
                            val decOutputs = decoderSession.outputInfo.keys
                            decoderOutputLogits = decOutputs.find { it.contains("logit", true) || it.contains("logits", true) } ?: decOutputs.first()
                        } catch (ex: Exception) {
                            Log.w(TAG, "Unable to resolve decoder IO names: ${ex.message}")
                        }
                        Log.d(TAG, "Resolved names -> encoderInput=$encoderInputName encoderOutput=$encoderOutputName decoderTokens=$decoderInputTokens decoderEncoder=$decoderInputEncoder decoderOutput=$decoderOutputLogits")
                        Log.d(TAG, "PhoWhisperONNX initialized successfully with encoder=$encPath decoder=$decPath (from cache files)")
                        initialized = true
                        break
                    } catch (e: Exception) {
                        Log.w(TAG, "Failed to initialize with encoder=$encPath decoder=$decPath: ${e.message}")
                        lastError = e
                    }
                }
                if (initialized) break
            }
            if (!initialized) {
                Log.e(TAG, "Failed to initialize PhoWhisperONNX with all candidate models: ${lastError?.message}", lastError)
                throw lastError ?: RuntimeException("PhoWhisperONNX failed to initialize: unknown error")
            }
            sessionsInitialized = true
        }
    }

    override suspend fun runEncoder(melSpectrogram: Array<FloatArray>): OnnxTensor = withContext(Dispatchers.Default) {
        Log.d(TAG, "runEncoder: start, mel shape=[${melSpectrogram.size}][${if (melSpectrogram.isNotEmpty()) melSpectrogram[0].size else 0}]")
        try {
            ensureSessionsInitialized() // Ensure sessions are initialized before running encoder
            val nMel = melSpectrogram.size
            val tSteps = melSpectrogram[0].size
            val flat = FloatArray(nMel * tSteps)
            var p = 0
            for (i in 0 until nMel) {
                val row = melSpectrogram[i]
                System.arraycopy(row, 0, flat, p, tSteps)
                p += tSteps
            }
            val shape = longArrayOf(1L, nMel.toLong(), tSteps.toLong())
            val tensor = OnnxTensor.createTensor(env, FloatBuffer.wrap(flat), shape)

            // Try a list of candidate input names (prioritize input_features)
            val encInputKeys = encoderSession.inputInfo.keys.toList()
            val candidateNames = (listOf("input_features", "input_feature", encoderInputName, "mel", "input") + encInputKeys).distinct()

            var lastRunException: Exception? = null
            var res: OrtSession.Result? = null
            for (name in candidateNames) {
                // Before trying, inspect the model's expected input shape for this name (if available).
                // The NodeInfo.info object may not expose a direct 'shape' property; use its toString() and parse numbers as a fallback.
                val expectedShapeNumbers: List<Long>? = try {
                    val infoStr = encoderSession.inputInfo[name]?.info?.toString()
                    if (infoStr != null) {
                        val nums = Regex("""-?\\d+""").findAll(infoStr).map { it.value.toLong() }.toList()
                        if (nums.size >= 3) nums.takeLast(3) else null
                    } else null
                } catch (ex: Exception) {
                    Log.w(TAG, "runEncoder: failed to parse expected shape info for input '$name': ${ex.message}")
                    null
                }
                Log.d(TAG, "runEncoder: candidate input '$name' parsed expectedShapeNumbers=$expectedShapeNumbers")

                // Helper to create a tensor matching expected orientation/length
                fun makeTensor(expectedT: Int?, transposed: Boolean): OnnxTensor {
                    if (!transposed) {
                        val targetT = expectedT ?: tSteps
                        val useT = if (tSteps >= targetT) targetT else targetT
                        val flat3 = FloatArray(nMel * useT)
                        // copy and pad/truncate per time axis
                        var p2 = 0
                        for (m in 0 until nMel) {
                            for (t in 0 until useT) {
                                val v = if (t < tSteps) melSpectrogram[m][t] else 0f
                                flat3[p2++] = v
                            }
                        }
                        val shape3 = longArrayOf(1L, nMel.toLong(), useT.toLong())
                        return OnnxTensor.createTensor(env, FloatBuffer.wrap(flat3), shape3)
                    } else {
                        val targetT = expectedT ?: tSteps
                        val useT = if (tSteps >= targetT) targetT else targetT
                        val flat2 = FloatArray(nMel * useT)
                        var idx = 0
                        for (t in 0 until useT) {
                            for (m in 0 until nMel) {
                                val v = if (t < tSteps) melSpectrogram[m][t] else 0f
                                flat2[idx++] = v
                            }
                        }
                        val shape2 = longArrayOf(1L, useT.toLong(), nMel.toLong())
                        return OnnxTensor.createTensor(env, FloatBuffer.wrap(flat2), shape2)
                    }
                }

                // Determine expected time dimension if model exposes it (using parsed numbers)
                var expectedTime: Int? = null
                var mustTranspose = false
                val expectedShape = expectedShapeNumbers
                if (expectedShape != null && expectedShape.size >= 3) {
                    // common pattern: [1, 80, 3000] or [1, 3000, 80]
                    if (expectedShape[1] == nMel.toLong()) {
                        if (expectedShape[2] > 0) expectedTime = expectedShape[2].toInt()
                        mustTranspose = false
                    } else if (expectedShape[2] == nMel.toLong()) {
                        if (expectedShape[1] > 0) expectedTime = expectedShape[1].toInt()
                        mustTranspose = true
                    } else {
                        // fallback heuristics: if one dim is large (>200) treat it as time
                        if (expectedShape[1] > 200) {
                            expectedTime = expectedShape[1].toInt()
                            mustTranspose = true
                        } else if (expectedShape[2] > 200) {
                            expectedTime = expectedShape[2].toInt()
                            mustTranspose = false
                        }
                    }
                }

                // Try non-transposed first (if expected indicates so), otherwise try both orders
                val tryOrders = if (mustTranspose) listOf(true, false) else listOf(false, true)

                for (trans in tryOrders) {
                     var tensorToTry: OnnxTensor? = null
                     try {
                         // create a tensor matching expected time length (pad/truncate)
                        tensorToTry = makeTensor(expectedTime, trans)
                        Log.d(TAG, "runEncoder: attempting encoderSession.run with input name='$name' transposed=$trans expectedTime=${expectedTime ?: "dynamic"} tensorInfo=${tensorToTry.info.toString()}")
                        val tmp = encoderSession.run(mapOf(name to tensorToTry))
                        res = tmp
                        Log.d(TAG, "runEncoder: succeeded with input name='$name' transposed=$trans")
                        tensorToTry.close()
                        break
                     } catch (e: Exception) {
                         lastRunException = e
                         Log.w(TAG, "runEncoder: attempt failed for input name='$name' transposed=$trans: ${e.message}")
                         // If the failure is due to invalid dimensions and the message contains an Expected: <N>, try one retry with that expected N
                        try {
                            val raw = e.toString()
                            Log.d(TAG, "runEncoder: raw exception string= $raw")
                            // more permissive regex: look for 'Expected' followed by non-digit(s) then digits
                            val expectedMatch = Regex("Expected\\D*(\\d+)").find(raw)
                            val expectedParsed = expectedMatch?.groups?.get(1)?.value?.toIntOrNull()
                             if (expectedParsed != null) {
                                 Log.d(TAG, "runEncoder: parsing expected time=$expectedParsed from error, will retry with padding/truncation (transposed=$trans)")
                                 var retryTensor: OnnxTensor? = null
                                 try {
                                     retryTensor = makeTensor(expectedParsed, trans)
                                     Log.d(TAG, "runEncoder: retrying encoderSession.run with expectedTime=$expectedParsed tensorInfo=${retryTensor.info.toString()}")
                                     val tmp2 = encoderSession.run(mapOf(name to retryTensor))
                                     res = tmp2
                                     Log.d(TAG, "runEncoder: retry succeeded with expectedTime=$expectedParsed transposed=$trans")
                                     retryTensor.close()
                                     tensorToTry?.close()
                                     break
                                 } catch (e2: Exception) {
                                     lastRunException = e2
                                     Log.w(TAG, "runEncoder: retry failed for expectedTime=$expectedParsed transposed=$trans: ${e2.message}")
                                     retryTensor?.close()
                                 }
                             }
                         } catch (_: Exception) {
                             // ignore parsing errors
                         } finally {
                             tensorToTry?.close()
                         }
                     }
                 }
                if (res != null) break
             }
             if (res == null) {
                 // none succeeded; throw the last exception
                 throw lastRunException ?: RuntimeException("Encoder run failed with unknown reason")
             }
            val outVal = res.get(0) as OnnxTensor
            // Extract output shape from info.toString() as fallback
            val outInfoStr = outVal.info.toString()
            val outNums = Regex("""-?\d+""").findAll(outInfoStr).map { it.value.toLong() }.toList()
            val shapeOut = if (outNums.isNotEmpty()) outNums.toLongArray() else outVal.info.toString().let { longArrayOf() }
            val data = outVal.floatArray
            val resultTensor: OnnxTensor = if (shapeOut.isNotEmpty()) {
                OnnxTensor.createTensor(env, FloatBuffer.wrap(data), shapeOut)
            } else {
                // fallback to 1D tensor matching data length
                OnnxTensor.createTensor(env, FloatBuffer.wrap(data), longArrayOf(data.size.toLong()))
            }

            // Cleanup
            tensor.close()
            outVal.close()
            res.close()

            Log.d(TAG, "runEncoder: done, output shape=${shapeOut.joinToString()}")
            resultTensor
        } catch (e: Exception) {
            Log.e(TAG, "runEncoder: error ${e.message}", e)
            throw e
        }
    }

    @Suppress("UNUSED_PARAMETER", "UNUSED_VARIABLE")
    override suspend fun runDecoderGreedy(encoderOutputs: OnnxTensor): IntArray = withContext(Dispatchers.Default) {
        Log.d(TAG, "runDecoderGreedy: start, encoder tensor info=${encoderOutputs.info.toString()}")
        try {
            ensureSessionsInitialized() // Ensure sessions are initialized before running decoder
            // Use safe accessors for start/end token ids to avoid immediate throw and provide better logs
            val startSeq = tokenizer.getStartSequenceSafe()
            val tokenList = ArrayList<Int>()
            val startToken: Int
            val endToken: Int
            if (startSeq != null && startSeq.isNotEmpty()) {
                tokenList.addAll(startSeq.toList())
                startToken = startSeq[0]
                endToken = tokenizer.getEndTokenIdSafe() ?: 50257 // fallback end token id
                Log.d(TAG, "runDecoderGreedy: using start sequence of length=${startSeq.size}, start=${startToken}, endFallback=${endToken}")
            } else {
                // fallback to single start token or numeric fallback
                startToken = tokenizer.getStartTokenIdSafe() ?: run {
                    Log.w(TAG, "runDecoderGreedy: start token not found in tokenizer, using numeric fallback 50258")
                    50258
                }
                endToken = tokenizer.getEndTokenIdSafe() ?: run {
                    Log.w(TAG, "runDecoderGreedy: end token not found in tokenizer, using numeric fallback 50257")
                    50257
                }
                tokenList.add(startToken)
                Log.d(TAG, "runDecoderGreedy: using single start token id=$startToken endToken=$endToken")
            }

            for (step in 0 until maxDecodeSteps) {
                val seqLen = tokenList.size
                val inputIdsBuffer = LongBuffer.allocate(seqLen)
                tokenList.forEach { inputIdsBuffer.put(it.toLong()) }
                inputIdsBuffer.rewind()
                val idsShape = longArrayOf(1L, seqLen.toLong())
                val inputIdsTensor = OnnxTensor.createTensor(env, inputIdsBuffer, idsShape)

                // Try candidate decoder input names (tokens / encoder output)
                val decInputKeys = decoderSession.inputInfo.keys.toList()
                val tokenCandidates = (listOf(decoderInputTokens, "input_ids", "input_ids_0", "tokens", "input_token", "ids") + decInputKeys).distinct()
                val encCandidates = (listOf(decoderInputEncoder, "encoder_output", "encoder", "encoder_outputs", "encoder_hidden") + decInputKeys).distinct()
                var out: OrtSession.Result? = null
                var logitsVal: OnnxTensor? = null
                var runEx: Exception? = null
                runCandidates@ for (tc in tokenCandidates) {
                    for (ec in encCandidates) {
                        try {
                            Log.d(TAG, "runDecoderGreedy: attempting decoderSession.run with token='$tc' encoder='$ec'")
                            val inputs = mapOf(tc to inputIdsTensor, ec to encoderOutputs)
                            val tmp = decoderSession.run(inputs)
                            out = tmp
                            logitsVal = out.get(0) as OnnxTensor
                            Log.d(TAG, "runDecoderGreedy: succeeded with token='$tc' encoder='$ec'")
                            break@runCandidates
                        } catch (e: Exception) {
                            runEx = e
                            Log.w(TAG, "runDecoderGreedy: attempt failed token='$tc' encoder='$ec': ${e.message}")
                        }
                    }
                }
                if (out == null || logitsVal == null) {
                    inputIdsTensor.close()
                    throw runEx ?: RuntimeException("Decoder run failed with unknown reason")
                }
                val logitsArray = logitsVal.floatArray
                val vocabSize = if (seqLen > 0 && logitsArray.size % seqLen == 0) logitsArray.size / seqLen else logitsArray.size
                val lastLogits = if (seqLen > 0 && logitsArray.size % seqLen == 0) {
                    val lastStart = (seqLen - 1) * vocabSize
                    logitsArray.copyOfRange(lastStart, lastStart + vocabSize)
                } else logitsArray

                // Argmax
                var maxIdx = 0
                var maxV = lastLogits[0]
                for (i in 1 until lastLogits.size) {
                    if (lastLogits[i] > maxV) {
                        maxV = lastLogits[i]
                        maxIdx = i
                    }
                }
                val nextToken = maxIdx

                // Debug: log the chosen token index and step
                val nextTokenStr = try { tokenizer.getTokenById(nextToken) } catch (_: Exception) { "" }
                Log.d(TAG, "runDecoderGreedy: step=$step nextToken=$nextToken token='$nextTokenStr' vocabSize=$vocabSize tokenListSize=${tokenList.size}")

                // Cleanup
                inputIdsTensor.close()
                logitsVal.close()
                out.close()

                // Robust stopping: if generated token equals endToken -> stop
                if (nextToken == endToken) {
                    Log.d(TAG, "runDecoderGreedy: reached endToken=$endToken at step=$step")
                    break
                }

                // Detect repeated-token stalls: if the same token repeats many times, stop
                // simplified repeat detection using last 3 tokens only
                // Maintain a small sliding window check to detect loops
                if (tokenList.size >= 3) {
                    val lastThree = tokenList.takeLast(3)
                    if (lastThree.all { it == nextToken }) {
                        Log.w(TAG, "runDecoderGreedy: detected repeating token=$nextToken 3x, breaking to avoid infinite loop")
                        break
                    }
                }

                tokenList.add(nextToken)

                // Safety limit: if token list grows too large, break and log
                if (tokenList.size >= 512) {
                    Log.w(TAG, "runDecoderGreedy: token list exceeded safety limit (size=${tokenList.size}), breaking")
                    break
                }
            }

            Log.d(TAG, "runDecoderGreedy: done, tokens=${tokenList.size}")
            tokenList.toIntArray()
        } catch (e: Exception) {
            Log.e(TAG, "runDecoderGreedy: error ${e.message}", e)
            throw e
        }
    }

    override fun decodeTokens(tokenIds: IntArray): String {
        Log.d(TAG, "decodeTokens: tokenCount=${tokenIds.size}")
        return tokenizer.decode(tokenIds.map { it.toLong() }.toLongArray())
    }

    override fun decodeVerbose(tokenIds: IntArray): Pair<String, List<String>> {
        val rawPieces = tokenIds.map { tokenizer.getTokenById(it) }
        val decoded = tokenizer.decode(tokenIds.map { it.toLong() }.toLongArray())
        return decoded to rawPieces
    }

    override fun isAvailable(): Boolean = true

    // Helper extension
    private val OnnxTensor.floatArray: FloatArray
        get() {
            return try {
                val buf = this.floatBuffer
                buf.rewind()
                val out = FloatArray(buf.remaining())
                buf.get(out)
                out
            } catch (e: Exception) {
                throw RuntimeException("Cannot extract float array from OnnxTensor: ${e.message}", e)
            }
        }
}
