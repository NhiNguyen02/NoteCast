package com.example.notecast.core.asr

import ai.onnxruntime.OrtEnvironment
import ai.onnxruntime.OrtSession
import ai.onnxruntime.TensorInfo
import android.content.Context
import android.util.Log
import com.example.notecast.utils.copyAssetToFile
import javax.inject.Inject
import javax.inject.Named
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext


/**
 * ONNXRuntimeManager
 * - Quản lý OrtEnvironment và hai OrtSession cho encoder/decoder PhoWhisper.
 * - Từ phiên bản này, session được khởi tạo lười (lazy) qua suspend fun initIfNeeded() trên IO
 *   để tránh block main thread trong init{} khi Hilt tạo graph.
 */
class ONNXRuntimeManager @Inject constructor(
    private val appContext: Context,
    @Named("IO") private val ioDispatcher: CoroutineDispatcher,
    private val encoderModelAssetPath: String = "phowhisper_base_onnx/encoder_model.onnx",
    private val decoderModelAssetPath: String = "phowhisper_base_onnx/decoder_model_merged.onnx",
) {
    private val env: OrtEnvironment = OrtEnvironment.getEnvironment()

    @Volatile
    private var encoderSessionInternal: OrtSession? = null

    @Volatile
    private var decoderSessionInternal: OrtSession? = null

    val encoderSession: OrtSession
        get() = checkNotNull(encoderSessionInternal) {
            "ONNX encoderSession is not initialized. Call initIfNeeded() from a suspend context before using."
        }

    val decoderSession: OrtSession
        get() = checkNotNull(decoderSessionInternal) {
            "ONNX decoderSession is not initialized. Call initIfNeeded() from a suspend context before using."
        }

    /**
     * Khởi tạo session nếu chưa có. Chạy toàn bộ I/O + createSession trên IO dispatcher.
     */
    suspend fun initIfNeeded() = withContext(ioDispatcher) {
        if (encoderSessionInternal != null && decoderSessionInternal != null) return@withContext

        // Double-check locking để tránh khởi tạo lại trong trường hợp chạy song song
        synchronized(this@ONNXRuntimeManager) {
            if (encoderSessionInternal != null && decoderSessionInternal != null) return@synchronized

            val encoderFile = copyAssetToFile(
                appContext,
                encoderModelAssetPath,
                "encoder_model.onnx"
            )
            val decoderFile = copyAssetToFile(
                appContext,
                decoderModelAssetPath,
                "decoder_model_merged.onnx"
            )

            val sessionOptions = createSessionOptions()
            encoderSessionInternal = env.createSession(encoderFile.absolutePath, sessionOptions)
            decoderSessionInternal = env.createSession(decoderFile.absolutePath, sessionOptions)

            // Log IO spec cho debug
            logSessionIO(encoderSessionInternal!!, "EncoderIO")
            logSessionIO(decoderSessionInternal!!, "DecoderIO")
        }
    }

    private fun createSessionOptions(): OrtSession.SessionOptions {
        return OrtSession.SessionOptions().apply {
            val cores = Runtime.getRuntime().availableProcessors()
            setIntraOpNumThreads(cores.coerceAtMost(4))
            setInterOpNumThreads(1)
        }
    }

    private fun logSessionIO(session: OrtSession, tag: String) {
        val inputInfo = session.inputInfo
        val outputInfo = session.outputInfo

        Log.d(tag, "===== INPUT INFO =====")
        for ((name, nodeInfo) in inputInfo) {
            val typeInfo = nodeInfo.info as? TensorInfo
            if (typeInfo != null) {
                Log.d(tag, "Input: $name")
                Log.d(tag, "  DType = ${typeInfo.type}")
                Log.d(tag, "  Shape = ${typeInfo.shape.joinToString()}")
            } else {
                Log.d(tag, "Input: $name (non-tensor type: ${nodeInfo.info})")
            }
        }

        Log.d(tag, "===== OUTPUT INFO =====")
        for ((name, nodeInfo) in outputInfo) {
            val typeInfo = nodeInfo.info as? TensorInfo
            if (typeInfo != null) {
                Log.d(tag, "Output: $name")
                Log.d(tag, "  DType = ${typeInfo.type}")
                Log.d(tag, "  Shape = ${typeInfo.shape.joinToString()}")
            } else {
                Log.d(tag, "Output: $name (non-tensor type: ${nodeInfo.info})")
            }
        }
    }

}
