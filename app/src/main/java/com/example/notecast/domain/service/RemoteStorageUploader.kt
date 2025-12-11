package com.example.notecast.domain.service

import java.io.File

/**
 * Abstraction for uploading audio files to a remote storage and returning a public download URL.
 *
 * Previously backed by Firebase Storage, now backed by Cloudinary.
 */
interface RemoteStorageUploader {
    /**
     * Upload given file and return a public HTTPS URL that backend can access.
     */
    suspend fun uploadAndGetUrl(file: File): String
}