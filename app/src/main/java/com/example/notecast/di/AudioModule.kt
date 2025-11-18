package com.example.notecast.di

import android.content.Context
import com.example.notecast.core.audio.AudioEngine
import com.example.notecast.core.vad.VADManager
import com.example.notecast.data.repository.AudioRepositoryImpl
import com.example.notecast.domain.repository.AudioRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AudioModule {

    @Binds
    @Singleton
    abstract fun bindAudioRepository(
        audioRepositoryImpl: AudioRepositoryImpl
    ): AudioRepository

    companion object {
        @Provides
        @Singleton
        fun provideAppScope(): CoroutineScope = CoroutineScope(Dispatchers.Default + SupervisorJob())

        @Provides
        @Singleton
        fun provideAudioEngine(scope: CoroutineScope): AudioEngine = AudioEngine(scope)

        @Provides
        @Singleton
        fun provideVADManager(@ApplicationContext context: Context): VADManager = VADManager(context, hangoverMs = 300)

        @Provides
        @Singleton
        @Named("IO")
        fun provideIoDispatcher(): CoroutineDispatcher = Dispatchers.IO
    }
}
