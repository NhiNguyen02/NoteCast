package com.example.notecast.di

import com.cloudinary.Cloudinary
import com.example.notecast.BuildConfig
import com.example.notecast.data.remote.service.CloudinaryStorageUploaderImpl
import com.example.notecast.domain.service.RemoteStorageUploader
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object CloudinaryModule {

    @Provides
    @Singleton
    fun provideCloudinary(): Cloudinary {
        val url = BuildConfig.CLOUDINARY_URL
        require(url.isNotBlank()) {
            "CLOUDINARY_URL must be defined in local.properties and exposed via BuildConfig.CLOUDINARY_URL"
        }
        return Cloudinary(url)
    }

    @Provides
    @Singleton
    fun provideRemoteStorageUploader(
        impl: CloudinaryStorageUploaderImpl
    ): RemoteStorageUploader = impl
}
