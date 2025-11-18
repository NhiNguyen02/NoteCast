package com.example.notecast.data.repository

import com.example.notecast.data.local.dao.FolderDao
import com.example.notecast.data.local.mapper.EntityMapper
import com.example.notecast.domain.model.Folder
import com.example.notecast.domain.repository.FolderRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FolderRepositoryImpl @Inject constructor(
    private val folderDao: FolderDao // <-- INJECT DAO THỨ 2
) : FolderRepository { // <-- Implement Interface từ Bước 1

    override fun getAllFolders(): Flow<List<Folder>> {
        return folderDao.getAllFolders().map { entities ->
            entities.map { EntityMapper.folderEntityToDomain(it) }
        }
    }

    override fun getFolderById(id: String): Flow<Folder?> {
        return folderDao.getFolderById(id).map { entity ->
            entity?.let { EntityMapper.folderEntityToDomain(it) }
        }
    }

    override suspend fun saveFolder(folder: Folder) {
        val entity = EntityMapper.domainToFolderEntity(folder).copy(
            isSynced = false // Đánh dấu là chưa đồng bộ
        )
        folderDao.upsertFolder(entity)
    }

    override suspend fun deleteFolder(id: String) {
        folderDao.softDeleteFolder(id, System.currentTimeMillis())
    }
}