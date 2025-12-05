package com.example.notecast.di

import android.content.Context
import com.example.notecast.core.asr.BpeTokenizerImpl
import com.example.notecast.core.asr.MelProcessor
import com.example.notecast.core.asr.ONNXRuntimeManager
import com.example.notecast.core.asr.PhoWhisperDecoderONNX
import com.example.notecast.core.asr.PhoWhisperEncoderONNX
import com.example.notecast.core.asr.Tokenizer
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * AsrModule
 * - Cấu hình Hilt cho các dependency liên quan đến ASR (PhoWhisper ONNX).
 */
@Module
@InstallIn(SingletonComponent::class)
object AsrModule {

    @Provides
    @Singleton
    fun provideOnnxRuntimeManager(@ApplicationContext context: Context): ONNXRuntimeManager {
        return ONNXRuntimeManager(context)
    }

    @Provides
    @Singleton
    fun provideMelProcessor(): MelProcessor = MelProcessor()

    @Provides
    @Singleton
    fun provideTokenizer(@ApplicationContext context: Context): Tokenizer {
        // Khởi tạo BpeTokenizerImpl từ tệp ngữ liệu trong assets/phowhisper_base_onnx
        val assetManager = context.assets
        return BpeTokenizerImpl.fromAssets(assetManager)
    }

    @Provides
    @Singleton
    fun providePhoWhisperEncoder(onnxManager: ONNXRuntimeManager): PhoWhisperEncoderONNX {
        return PhoWhisperEncoderONNX(onnxManager)
    }

    @Provides
    @Singleton
    fun providePhoWhisperDecoder(
        onnxManager: ONNXRuntimeManager,
        tokenizer: Tokenizer,
    ): PhoWhisperDecoderONNX {
        return PhoWhisperDecoderONNX(onnxManager, tokenizer)
    }
}
