package com.example.notecast.core.asr

import ai.onnxruntime.OnnxTensor
import ai.onnxruntime.OrtEnvironment
import java.nio.FloatBuffer

/**
 * PhoWhisperEncoderONNX
 * - Bọc encoder.onnx: nhận log-mel [80, T] và chạy encoder.
 * - TODO: đối chiếu tên input/output với docs/onnx-model-io-signatures-2025-12-03.md
 *   (ví dụ: input "input_features", output "last_hidden_state").
 */
class PhoWhisperEncoderONNX(
    private val onnxManager: ONNXRuntimeManager,
) {
    data class EncoderOutput(
        /**
         * Flattened encoder hidden states [T_enc * 512].
         */
        val hiddenStates: FloatArray,
        /**
         * Số frame encoder T_enc (time dimension sau encoder).
         */
        val length: Int,
    )

    /**
     * Suspend wrapper đảm bảo ONNX sessions đã được init trước khi chạy encoder.
     */
    suspend fun runWithInit(mel: FloatArray, nFrames: Int): EncoderOutput {
        onnxManager.initIfNeeded()
        return runInternal(mel, nFrames)
    }

    /**
     * @param mel flat FloatArray [80 * 3000] (đã pad/truncate bên ngoài)
     * @param nFrames số frame mel thực tế (T_real <= 3000) để trả về cho decoder.
     */
    fun runInternal(mel: FloatArray, nFrames: Int): EncoderOutput {
        val env = OrtEnvironment.getEnvironment()
        // Model encoder hiện tại mong đợi đúng T=3000 ở dimension 2
        val shape = longArrayOf(1, 80, 3000L)
        val tensor = OnnxTensor.createTensor(env, FloatBuffer.wrap(mel), shape)

        // Theo IO spec: input "input_features", output "last_hidden_state" [1, T_enc, 512]
        val outputs = onnxManager.encoderSession.run(mapOf("input_features" to tensor))

        // TODO: nếu cần, lấy output theo tên "last_hidden_state" thay vì chỉ số 0.
        @Suppress("UNCHECKED_CAST")
        val out3d = outputs[0].value as Array<Array<FloatArray>> // [1, T_enc_model, 512]
        val hidden2d = out3d[0] // [T_enc_model, 512]
        // T_enc thực tế có thể nhỏ hơn T_enc_model, nhưng với PhoWhisper, T_enc ≈ T_real/2.
        // Ở đây ta tin cậy length mà encoder trả về theo chiều time đầu tiên của output.
        val tEnc = hidden2d.size // số frame encoder T_enc

        // Flatten [T_enc, 512] về [T_enc * 512] để thuận tiện cho decoder.
        val hiddenFlat = FloatArray(tEnc * 512)
        var dst = 0
        for (t in 0 until tEnc) {
            val row = hidden2d[t]
            // Bảo vệ: nếu row.length != 512 (trường hợp edge), copy min.
            val len = minOf(row.size, 512)
            row.copyInto(hiddenFlat, destinationOffset = dst, startIndex = 0, endIndex = len)
            dst += 512
        }

        tensor.close()
        outputs.close()

        return EncoderOutput(hiddenStates = hiddenFlat, length = tEnc)
    }


}
