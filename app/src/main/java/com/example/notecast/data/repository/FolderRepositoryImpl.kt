package com.example.notecast.data.repository

import com.example.notecast.data.local.dao.FolderDao
import com.example.notecast.data.local.mapper.MappingEntityToDomain
import com.example.notecast.data.remote.FolderRemoteDataSource
import com.example.notecast.domain.model.Folder
import com.example.notecast.domain.repository.FolderRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.util.UUID
import javax.inject.Inject

/**
 * Implementation của FolderRepository: Room là source-of-truth, remote để sync với NoteServices.
 */
class FolderRepositoryImpl @Inject constructor(
    private val folderDao: FolderDao,
    private val remote: FolderRemoteDataSource,
) : FolderRepository {

    override fun getAllFolders(): Flow<List<Folder>> {
        // Khi lần đầu collect, trigger một lần sync từ backend về Room
        return folderDao.getAllFolders()
            .map { entities ->
                // map Room -> Domain
                entities.map { MappingEntityToDomain.folderEntityToDomain(it) }
            }
    }

    override fun getFolderById(id: String): Flow<Folder?> =
        folderDao.getFolderById(id).map { it?.let(MappingEntityToDomain::folderEntityToDomain) }

    override suspend fun saveFolder(folder: Folder) {
        withContext(Dispatchers.IO) {
            val now = System.currentTimeMillis()
            val localId = folder.id.ifBlank { UUID.randomUUID().toString() }
            val toSave = folder.copy(id = localId, updatedAt = now)

            // Save to Room
            folderDao.upsertFolder(MappingEntityToDomain.domainToFolderEntity(toSave.copy(isSynced = false)))

            // Sync lên server: tạo mới hoặc cập nhật
            val remoteFolder = if (folder.id.isBlank()) {
                remote.createFolder(toSave)
            } else {
                remote.updateFolder(toSave)
                toSave
            }

            // Cập nhật lại Room đánh dấu isSynced
            folderDao.upsertFolder(
                MappingEntityToDomain.domainToFolderEntity(
                    remoteFolder.copy(isSynced = true)
                )
            )
        }
    }

    override suspend fun deleteFolder(id: String) {
        withContext(Dispatchers.IO) {
            val now = System.currentTimeMillis()
            folderDao.softDeleteFolder(id, now)
            remote.deleteFolder(id)
        }
    }

    /**
     * Hàm tiện ích: đồng bộ toàn bộ folders từ backend về Room (dùng khi app mở).
     */
    suspend fun syncFromRemoteOnce() {
        withContext(Dispatchers.IO) {
            val remoteFolders = remote.fetchAllFolders()
            remoteFolders.forEach { folder ->
                folderDao.upsertFolder(
                    MappingEntityToDomain.domainToFolderEntity(
                        folder.copy(isSynced = true, isDeleted = false)
                    )
                )
            }
        }
    }
}
