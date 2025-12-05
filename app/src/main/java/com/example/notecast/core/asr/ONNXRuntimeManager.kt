package com.example.notecast.core.asr

import ai.onnxruntime.OrtEnvironment
import ai.onnxruntime.OrtSession
import android.content.Context
import com.example.notecast.utils.copyAssetToFile

/**
 * ONNXRuntimeManager
 * - Khởi tạo OrtEnvironment và hai OrtSession cho encoder/decoder PhoWhisper.
 * - Ẩn chi tiết đường dẫn model, options delegate.
 */
class ONNXRuntimeManager(
    context: Context,
    encoderModelAssetPath: String = "phowhisper_base_onnx/encoder_model.onnx",
    decoderModelAssetPath: String = "phowhisper_base_onnx/decoder_model_merged.onnx",
) {
    private val env: OrtEnvironment = OrtEnvironment.getEnvironment()

    val encoderSession: OrtSession
    val decoderSession: OrtSession

    init {
        val encoderFile = copyAssetToFile(
            context,
            encoderModelAssetPath,
            "encoder_model.onnx"
        )
        val decoderFile = copyAssetToFile(
            context,
            decoderModelAssetPath,
            "decoder_model_merged.onnx"
        )

        // Dùng SessionOptions với thiết lập an toàn (CPU, multi-thread hạn chế).
        val sessionOptions = createSessionOptions()
        encoderSession = env.createSession(encoderFile.absolutePath, sessionOptions)
        decoderSession = env.createSession(decoderFile.absolutePath, sessionOptions)
    }

    private fun createSessionOptions(): OrtSession.SessionOptions {
        return OrtSession.SessionOptions().apply {
            val cores = Runtime.getRuntime().availableProcessors()

            // Sử dụng tối đa 4 thread cho intra-op để tránh quá tải CPU trên mobile.
            setIntraOpNumThreads(cores.coerceAtMost(4))

            // inter-op nhỏ vì thường chỉ chạy 1 session tại một thời điểm.
            setInterOpNumThreads(1)

            // TODO: nếu cần tối ưu sâu hơn, đọc lại API OrtSession.SessionOptions
            // của phiên bản onnxruntime đang dùng (graph optimization level, memory arena...).
            // TODO: nếu cần tăng tốc, cân nhắc bật NNAPI / GPU delegate cho Android
            // sau khi đo benchmark thực tế.
        }
    }
}
