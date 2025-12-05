package com.example.notecast.domain.usecase

import com.example.notecast.core.asr.Tokenizer
import javax.inject.Inject
import javax.inject.Named
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

/**
 * WarmupAsrUseCase
 * - Hiện tại chỉ dùng để load tokenizer.json/vocab/merges sớm trên IO.
 * - Tránh gọi encoder/decoder ONNX ở startup để không block main thread.
 */
class WarmupAsrUseCase @Inject constructor(
    private val tokenizer: Tokenizer,
    @Named("IO") private val ioDispatcher: CoroutineDispatcher,
) {
    /**
     * Thực hiện warmup trên IO dispatcher, nuốt mọi lỗi (chỉ để pre-load, không ảnh hưởng UX nếu fail).
     */
    suspend operator fun invoke() = withContext(ioDispatcher) {
        try {
            // 1. Gọi nhẹ tokenizer để chắc chắn đã load tokenizer.json/vocab/merges.
            tokenizer.encode("")
        } catch (_: Throwable) {
            // Warmup failure should not crash app; real ASR sẽ retry khi dùng thật.
        }
    }
}
