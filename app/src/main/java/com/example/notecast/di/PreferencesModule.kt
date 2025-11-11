package com.example.notecast.di

import com.example.notecast.data.repository.PreferencesRepositoryImpl
import com.example.notecast.domain.repository.PreferencesRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class PreferencesModule {

    /**
     * Binds PreferencesRepository Interface với Implementation.
     * Hilt sử dụng cái này để tiêm vào MainActivity và các nơi khác.
     */
    @Binds
    @Singleton
    abstract fun bindPreferencesRepository(
        preferencesRepositoryImpl: PreferencesRepositoryImpl
    ): PreferencesRepository
}