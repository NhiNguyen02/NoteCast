package com.example.notecast.di

import android.content.Context
import androidx.room.Room
import com.example.notecast.data.local.AppDatabase
import com.example.notecast.data.local.migration.MIGRATION_1_2
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module providing Room database and DAOs.
 */
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(appContext: Context): AppDatabase {
        return Room.databaseBuilder(appContext, AppDatabase::class.java, "notecast.db")
            .addMigrations(MIGRATION_1_2)
            .fallbackToDestructiveMigration(false) // remove or adjust in production
            .build()
    }

    @Provides
    fun provideAudioDao(db: AppDatabase) = db.audioDao()

    @Provides
    fun provideTranscriptDao(db: AppDatabase) = db.transcriptDao()

    @Provides
    fun provideProcessedTextDao(db: AppDatabase) = db.processedTextDao()

    @Provides
    fun provideNoteDao(db: AppDatabase) = db.noteDao()

    @Provides
    fun provideFolderDao(db: AppDatabase) = db.folderDao()
}