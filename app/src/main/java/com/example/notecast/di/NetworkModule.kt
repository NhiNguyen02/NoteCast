package com.example.notecast.di

import com.example.notecast.BuildConfig
import com.example.notecast.data.remote.GeminiApiService
import com.example.notecast.data.remote.PhoWhisperApiService
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
import javax.inject.Qualifier
import javax.inject.Singleton

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class GeminiClient

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class PhoWhisperClient

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    // Shared Json configuration for all Retrofit services
    @Provides
    @Singleton
    fun provideJson(): Json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    @Provides
    @Singleton
    @GeminiClient
    fun provideGeminiOkHttpClient(): OkHttpClient {
        val logging = HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) {
                HttpLoggingInterceptor.Level.BODY
            } else {
                HttpLoggingInterceptor.Level.BASIC
            }
        }
        return OkHttpClient.Builder()
            .addInterceptor(logging)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    @Provides
    @Singleton
    @PhoWhisperClient
    fun providePhoWhisperOkHttpClient(): OkHttpClient {
        val logging = HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) {
                HttpLoggingInterceptor.Level.BODY
            } else {
                HttpLoggingInterceptor.Level.BASIC
            }
        }
        return OkHttpClient.Builder()
            .addInterceptor(logging)
            // PhoWhisper có thể xử lý audio dài (tới 10 phút), cần timeout cao hơn
            .connectTimeout(60, TimeUnit.SECONDS)
            .readTimeout(180, TimeUnit.SECONDS)
            .build()
    }

    @Provides
    @Singleton
    fun provideGeminiApiService(
        @GeminiClient client: OkHttpClient,
        json: Json
    ): GeminiApiService {
        val contentType = "application/json".toMediaType()

        return Retrofit.Builder()
            .baseUrl("https://generativelanguage.googleapis.com/") // Base URL chuẩn
            .client(client)
            .addConverterFactory(json.asConverterFactory(contentType))
            .build()
            .create(GeminiApiService::class.java)
    }

    @Provides
    @Singleton
    fun providePhoWhisperApiService(
        @PhoWhisperClient client: OkHttpClient,
        json: Json
    ): PhoWhisperApiService {
        val contentType = "application/json".toMediaType()

        return Retrofit.Builder()
            .baseUrl("https://bichnhan2701-PhoWhisperBaseAPI.hf.space/")
            .client(client)
            .addConverterFactory(json.asConverterFactory(contentType))
            .build()
            .create(PhoWhisperApiService::class.java)
    }
}