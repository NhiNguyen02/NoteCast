package com.example.notecast.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import dagger.hilt.android.qualifiers.ApplicationContext
import android.util.Log
import com.example.notecast.core.asr.ASRCore
import com.example.notecast.core.asr.NoopPhoWhisper
import com.example.notecast.core.asr.PhoWhisperEngine
import com.example.notecast.core.asr.PhoWhisperONNX
import com.example.notecast.data.repository.ASRRepositoryImpl
import com.example.notecast.domain.repository.ASRRepository
import dagger.Binds


@Module
@InstallIn(SingletonComponent::class)
abstract class ASRModule {

    // Bind the ASRRepository implementation instead of AudioRepository to avoid duplicate bindings
    @Binds
    @Singleton
    abstract fun bindASRRepository(
        asrRepositoryImpl: ASRRepositoryImpl
    ): ASRRepository

    companion object {
        @Provides
        @Singleton
        fun providePhoWhisperONNX(@ApplicationContext context: Context): PhoWhisperEngine {
            return try {
                // Explicitly use the non-quantized PhoWhisper ONNX models located under assets
                PhoWhisperONNX(
                    context,
                    encoderAssetPath = "phowhisper-base-onnx/encoder_model.onnx",
                    decoderAssetPath = "phowhisper-base-onnx/decoder_model.onnx"
                )
            } catch (e: Exception) {
                Log.e("ASRModule", "PhoWhisperONNX failed to initialize, falling back to Noop: ${e.message}")
                NoopPhoWhisper()
            }
        }

        @Provides
        @Singleton
        fun provideASRCore(phoWhisper: PhoWhisperEngine): ASRCore {
            return ASRCore(phoWhisper)
        }
    }
}