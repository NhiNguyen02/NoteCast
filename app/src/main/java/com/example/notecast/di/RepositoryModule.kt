package com.example.notecast.di

import com.example.notecast.data.local.dao.FolderDao
import com.example.notecast.data.local.dao.NoteDao
import com.example.notecast.data.remote.FolderRemoteDataSource
import com.example.notecast.data.repository.AudioRepositoryImpl
import com.example.notecast.data.repository.FolderRepositoryImpl
import com.example.notecast.data.repository.NoteRepositoryImpl
import com.example.notecast.data.repository.PreferencesRepositoryImpl
import com.example.notecast.data.repository.RemoteNoteServiceRepositoryImpl
import com.example.notecast.domain.repository.AudioRepository
import com.example.notecast.domain.repository.FolderRepository
import com.example.notecast.domain.repository.NoteRemoteRepository
import com.example.notecast.domain.repository.NoteRepository
import com.example.notecast.domain.repository.PreferencesRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Provides
    @Singleton
    fun provideNoteRepository(
        noteDao: NoteDao,
        noteRemoteRepository: NoteRemoteRepository,
    ): NoteRepository = NoteRepositoryImpl(noteDao, noteRemoteRepository)

    @Provides
    @Singleton
    fun provideFolderRepository(
        folderDao: FolderDao,
        remote: FolderRemoteDataSource,
    ): FolderRepository = FolderRepositoryImpl(folderDao, remote)


    @Provides
    @Singleton
    fun providePreferencesRepository(
        impl: PreferencesRepositoryImpl
    ): PreferencesRepository = impl

    @Provides
    @Singleton
    fun provideAudioRepository(
        impl: AudioRepositoryImpl
    ): AudioRepository = impl

    @Provides
    @Singleton
    fun provideRemoteNoteServiceRepository(
        impl: RemoteNoteServiceRepositoryImpl
    ): NoteRemoteRepository = impl
}