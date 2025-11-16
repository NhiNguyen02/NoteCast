package com.example.notecast.di

import android.content.Context
import androidx.room.Room
import com.example.notecast.data.local.AppDatabase
import com.example.notecast.data.local.dao.AudioDao
import com.example.notecast.data.local.dao.FolderDao
import com.example.notecast.data.local.dao.NoteDao
import com.example.notecast.data.local.dao.ProcessedTextDao
import com.example.notecast.data.local.dao.TranscriptDao
import com.example.notecast.data.local.migration.MIGRATION_1_2
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module cung cấp Room database và 5 DAOs.
 */
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext appContext: Context): AppDatabase {
        return Room.databaseBuilder(appContext, AppDatabase::class.java, "notecast.db")
            .addMigrations(MIGRATION_1_2)
            .fallbackToDestructiveMigration(false)
            .build()
    }

    @Provides
    @Singleton // Đảm bảo DAOs cũng là Singleton
    fun provideAudioDao(db: AppDatabase): AudioDao = db.audioDao()

    @Provides
    @Singleton
    fun provideTranscriptDao(db: AppDatabase): TranscriptDao = db.transcriptDao()

    @Provides
    @Singleton
    fun provideProcessedTextDao(db: AppDatabase): ProcessedTextDao = db.processedTextDao()

    @Provides
    @Singleton
    fun provideNoteDao(db: AppDatabase): NoteDao = db.noteDao()

    @Provides
    @Singleton
    fun provideFolderDao(db: AppDatabase): FolderDao = db.folderDao()
}