package com.example.notecast.di

import com.example.notecast.data.repository.AsrRepositoryImpl
import com.example.notecast.data.repository.AudioRepositoryImpl
import com.example.notecast.data.repository.FolderRepositoryImpl
import com.example.notecast.data.repository.NoteRepositoryImpl
import com.example.notecast.data.repository.PreferencesRepositoryImpl
import com.example.notecast.data.repository.SummaryRepository
import com.example.notecast.data.repository.VADRepositoryImpl
import com.example.notecast.domain.repository.AsrRepository
import com.example.notecast.domain.repository.NoteRepository
import com.example.notecast.domain.repository.PreferencesRepository
import com.example.notecast.domain.repository.AudioRepository
import com.example.notecast.domain.repository.FolderRepository
import com.example.notecast.domain.repository.VADRepository
import com.example.notecast.domain.usecase.SummarizeNoteUseCase
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindNoteRepository(
        impl: NoteRepositoryImpl
    ): NoteRepository // Ràng buộc Interface và Implementation

    @Binds
    @Singleton
    abstract fun bindFolderRepository(
        impl: FolderRepositoryImpl
    ): FolderRepository // Ràng buộc Interface và Implementation

    @Binds
    @Singleton
    abstract fun bindPreferencesRepository(
        impl: PreferencesRepositoryImpl
    ): PreferencesRepository // Ràng buộc Interface và Implementation

    @Binds
    @Singleton
    abstract fun bindAudioRepository(
        impl: AudioRepositoryImpl // Trỏ đến file Impl mới sửa
    ): AudioRepository

    @Binds
    @Singleton
    abstract fun bindAsrRepository(
        impl: AsrRepositoryImpl
    ): AsrRepository

    @Binds
    @Singleton
    abstract fun bindVADRepository(
        impl: VADRepositoryImpl
    ): VADRepository

    companion object {

        @Provides
        @Singleton
        fun provideSummaryRepository(
            apiService: com.example.notecast.data.remote.GeminiApiService
        ): SummaryRepository = SummaryRepository(apiService)

        @Provides
        @Singleton
        fun provideSummarizeNoteUseCase(
            summaryRepository: SummaryRepository
        ): SummarizeNoteUseCase = SummarizeNoteUseCase(summaryRepository)
    }
}