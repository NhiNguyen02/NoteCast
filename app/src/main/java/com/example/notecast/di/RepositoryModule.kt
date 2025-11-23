package com.example.notecast.di

import com.example.notecast.data.repository.FolderRepositoryImpl
import com.example.notecast.data.repository.NoteRepositoryImpl
import com.example.notecast.data.repository.PreferencesRepositoryImpl
import com.example.notecast.data.repository.RecorderRepositoryImpl
import com.example.notecast.domain.repository.FolderRepository
import com.example.notecast.domain.repository.NoteRepository
import com.example.notecast.domain.repository.PreferencesRepository
import com.example.notecast.domain.repository.RecorderRepository
import dagger.Binds
import dagger.Module
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
    abstract fun bindRecorderRepository(
        impl: RecorderRepositoryImpl // Trỏ đến file Impl mới sửa
    ): RecorderRepository
}