package com.example.notecast.di

import android.content.Context
import com.example.notecast.core.audio.AudioBuffer
import com.example.notecast.core.audio.AudioEngine
import com.example.notecast.core.audio.AudioRecorder
import com.example.notecast.core.audio.RingAudioBuffer
import com.example.notecast.core.vad.VADManager
import com.example.notecast.core.vad.Segmenter
import com.example.notecast.data.vad.rms.RmsVADImpl
import com.example.notecast.domain.vad.VADDetector
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AudioModule {

    companion object {
        @Provides
        @Singleton
        fun provideAudioRecorder(): AudioRecorder = AudioRecorder()

        @Provides
        @Singleton
        @Named("vadBuffer")
        fun provideVadBuffer(): AudioBuffer<ShortArray> = RingAudioBuffer(capacityFrames = 50) // ~1s nếu frame 20ms

        @Provides
        @Singleton
        @Named("asrBuffer")
        fun provideAsrBuffer(): AudioBuffer<ShortArray> = RingAudioBuffer(capacityFrames = 200)

        @Provides
        @Singleton
        @Named("recorderBuffer")
        fun provideRecorderBuffer(): AudioBuffer<ShortArray> = RingAudioBuffer(capacityFrames = 200)

        @Provides
        @Singleton
        fun provideAudioEngine(
            recorder: AudioRecorder,
            @Named("vadBuffer") vadBuffer: AudioBuffer<ShortArray>,
            @Named("asrBuffer") asrBuffer: AudioBuffer<ShortArray>,
            @Named("recorderBuffer") recorderBuffer: AudioBuffer<ShortArray>,
            @ApplicationContext context: Context
        ): AudioEngine = AudioEngine(
            recorder = recorder,
            vadBuffer = vadBuffer,
            asrBuffer = asrBuffer,
            recorderBuffer = recorderBuffer,
            appContext = context
        )


        @Provides
        @Singleton
        fun provideVADManager(): VADManager = VADManager()

        @Provides
        @Singleton
        @Named("IO")
        fun provideIoDispatcher(): CoroutineDispatcher = Dispatchers.IO

        @Provides
        @Singleton
        fun provideVADDetector(): VADDetector = RmsVADImpl(frameSize = 320)

        @Provides
        @Singleton
        fun provideSegmenter(): Segmenter = Segmenter(
            sampleRate = 16_000,
            frameSize = 320,
            hangoverMs = 300,
            prerollMs = 150,
        )
    }
}
