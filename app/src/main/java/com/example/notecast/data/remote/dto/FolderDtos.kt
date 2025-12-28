package com.example.notecast.data.remote.dto

import kotlinx.serialization.Serializable

/**
 * DTO cho Folder APIs của NoteServices, align với noteservices_api_endpoints.md
 */

@Serializable
data class FolderDto(
    val folder_id: String,
    val name: String,
    val color_hex: String? = null,
    val created_at: Long? = null,
)

@Serializable
data class FolderCreateRequest(
    val name: String,
    val color_hex: String? = null,
)

@Serializable
data class FolderCreateResponse(
    val folder_id: String,
    val name: String,
    val color_hex: String? = null,
    val created_at: Long,
)

@Serializable
data class FolderUpdateRequest(
    val name: String? = null,
    val color_hex: String? = null,
)

@Serializable
data class FolderUpdateResponse(
    val folder_id: String,
    val updated: Boolean,
)

@Serializable
data class FolderDeleteResponse(
    val folder_id: String,
    val deleted: Boolean,
)

