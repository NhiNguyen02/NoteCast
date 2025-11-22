package com.example.notecast.di

import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module(includes = [
    RepositoryModule::class, // <-- Bắt buộc
    PreferencesModule::class, // <-- Bắt buộc
    AudioModule::class,       // <-- Bắt buộc
    ASRModule::class,
    // Thêm các Module khác (ví dụ: NetworkModule, DatabaseModule) vào đây
])
@InstallIn(SingletonComponent::class)
object AppModule {
    // Không cần hàm @Provides ở đây nếu tất cả là @Binds trong các Module con
}