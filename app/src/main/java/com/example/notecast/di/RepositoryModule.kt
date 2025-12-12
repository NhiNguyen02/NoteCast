package com.example.notecast.di

import com.example.notecast.data.repository.FolderRepositoryImpl
import com.example.notecast.data.repository.NoteRepositoryImpl
import com.example.notecast.data.repository.PreferencesRepositoryImpl
import com.example.notecast.data.repository.RecorderRepositoryImpl
import com.example.notecast.data.repository.SummaryRepository
import com.example.notecast.domain.repository.FolderRepository
import com.example.notecast.domain.repository.NoteRepository
import com.example.notecast.domain.repository.PreferencesRepository
import com.example.notecast.domain.repository.RecorderRepository
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
    ): NoteRepository

    @Binds
    @Singleton
    abstract fun bindFolderRepository(
        impl: FolderRepositoryImpl
    ): FolderRepository

    @Binds
    @Singleton
    abstract fun bindPreferencesRepository(
        impl: PreferencesRepositoryImpl
    ): PreferencesRepository

    @Binds
    @Singleton
    abstract fun bindRecorderRepository(
        impl: RecorderRepositoryImpl
    ): RecorderRepository

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
