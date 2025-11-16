package com.example.notecast.di

// SỬA: Import Implementation mới
import com.example.notecast.data.repository.AudioRepositoryImpl
import com.example.notecast.data.repository.NoteRepositoryImpl
// SỬA: Import Interface mới
import com.example.notecast.domain.repository.AudioRepository
import com.example.notecast.domain.repository.NoteRepository
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
        noteRepositoryImpl: NoteRepositoryImpl
    ): NoteRepository

    // SỬA: Thêm Binds cho AudioRepository
    @Binds
    @Singleton
    abstract fun bindAudioRepository(
        audioRepositoryImpl: AudioRepositoryImpl
    ): AudioRepository

    // TODO: Thêm Binds cho FolderRepository...
}