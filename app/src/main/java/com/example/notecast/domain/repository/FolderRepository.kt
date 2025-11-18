package com.example.notecast.domain.repository

import com.example.notecast.domain.model.Folder
import kotlinx.coroutines.flow.Flow

interface FolderRepository {
    fun getAllFolders(): Flow<List<Folder>>
    fun getFolderById(id: String): Flow<Folder?>
    suspend fun saveFolder(folder: Folder) // (Tạo mới hoặc Cập nhật)
    suspend fun deleteFolder(id: String) // (Xóa mềm)
}