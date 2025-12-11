package com.example.notecast.data.remote.service

import com.cloudinary.Cloudinary
import com.cloudinary.utils.ObjectUtils
import com.example.notecast.domain.service.RemoteStorageUploader
import java.io.File
import javax.inject.Inject

/**
 * Triển khai RemoteStorageUploader sử dụng Cloudinary thay vì Firebase Storage.
 *
 * Yêu cầu: cấu hình Cloudinary trong local.properties hoặc BuildConfig (CLOUDINARY_URL hoặc 3 biến riêng biệt).
 */
class CloudinaryStorageUploaderImpl @Inject constructor(
    private val cloudinary: Cloudinary
) : RemoteStorageUploader {

    override suspend fun uploadAndGetUrl(file: File): String {
        // Upload đồng bộ; Cloudinary SDK không có suspend nên dùng runCatching + blocking
        val result = cloudinary.uploader().upload(
            file,
            ObjectUtils.asMap(
                "resource_type", "auto",
                "folder", "recordings"
            )
        )
        @Suppress("UNCHECKED_CAST")
        val url = result["secure_url"] as? String
        return url ?: throw IllegalStateException("Cloudinary upload missing secure_url")
    }
}
