package com.example.notecast.data.remote.mapping

import com.example.notecast.data.remote.dto.FolderCreateRequest
import com.example.notecast.data.remote.dto.FolderDto
import com.example.notecast.data.remote.dto.FolderUpdateRequest
import com.example.notecast.domain.model.Folder

/** Folder API ↔ Domain mapper */

fun FolderDto.toDomain(): Folder = Folder(
    id = folder_id,
    name = name,
    colorHex = color_hex,
    createdAt = created_at ?: 0L,
    updatedAt = created_at ?: 0L, // backend chưa có updated_at, tạm gán created_at
    isSynced = true,
    isDeleted = false,
)

fun Folder.toCreateRequest(): FolderCreateRequest =
    FolderCreateRequest(
        name = name,
        color_hex = colorHex,
    )

fun Folder.toUpdateRequest(): FolderUpdateRequest =
    FolderUpdateRequest(
        name = name,
        color_hex = colorHex,
    )

