package com.example.notecast.di

import com.example.notecast.data.remote.GeminiApiService

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        return OkHttpClient.Builder()
            .addInterceptor(logging)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    @Provides
    @Singleton
    fun provideGeminiApiService(client: OkHttpClient): GeminiApiService {
        val contentType = "application/json".toMediaType()
        val json = Json { ignoreUnknownKeys = true; isLenient = true }

        return Retrofit.Builder()
            .baseUrl("https://generativelanguage.googleapis.com/") // Base URL chuẩn
            .client(client)
            .addConverterFactory(json.asConverterFactory(contentType))
            .build()
            .create(GeminiApiService::class.java)
    }
}