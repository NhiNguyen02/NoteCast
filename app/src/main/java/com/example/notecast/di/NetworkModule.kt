package com.example.notecast.di

import com.example.notecast.BuildConfig
import com.example.notecast.data.remote.NoteEventsSseClient
import com.example.notecast.data.remote.NoteServiceApi
import com.example.notecast.data.remote.PhoWhisperApi
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

// Chỉ giữ client cho PhoWhisper & NoteService; LLM (Gemini) đã được tách sang backend.

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class PhoWhisperClient

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class NoteServiceClient

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideJson(): Json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    @Provides
    @Singleton
    @PhoWhisperClient
    fun providePhoWhisperOkHttpClient(): OkHttpClient {
        val logging = HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BODY
            else HttpLoggingInterceptor.Level.BASIC
        }
        return OkHttpClient.Builder()
            .addInterceptor(logging)
            .connectTimeout(60, TimeUnit.SECONDS)
            .readTimeout(180, TimeUnit.SECONDS)
            .build()
    }

    @Provides
    @Singleton
    @NoteServiceClient
    fun provideNoteServiceOkHttpClient(): OkHttpClient {
        val logging = HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BODY
            else HttpLoggingInterceptor.Level.BASIC
        }
        return OkHttpClient.Builder()
            .addInterceptor(logging)
            .connectTimeout(60, TimeUnit.SECONDS)
            .readTimeout(180, TimeUnit.SECONDS)
            .build()
    }

    @Provides
    @Singleton
    fun providePhoWhisperApi(
        @PhoWhisperClient client: OkHttpClient,
        json: Json
    ): PhoWhisperApi {
        val contentType = "application/json".toMediaType()
        return Retrofit.Builder()
            .baseUrl(BuildConfig.PHO_WHISPER_API_BASE_URL)
            .client(client)
            .addConverterFactory(json.asConverterFactory(contentType))
            .build()
            .create(PhoWhisperApi::class.java)
    }

    @Provides
    @Singleton
    fun provideNoteServiceApi(
        @NoteServiceClient client: OkHttpClient,
        json: Json
    ): NoteServiceApi {
        val contentType = "application/json".toMediaType()

        return Retrofit.Builder()
            .baseUrl(BuildConfig.NOTE_SERVICE_URL)
            .client(client)
            .addConverterFactory(json.asConverterFactory(contentType))
            .build()
            .create(NoteServiceApi::class.java)
    }

    @Provides
    @Singleton
    fun provideNoteEventsSseClient(
        @NoteServiceClient client: OkHttpClient,
    ): NoteEventsSseClient = NoteEventsSseClient(
        baseUrl = BuildConfig.NOTE_SERVICE_URL.trimEnd('/'),
        okHttpClient = client,
    )
}