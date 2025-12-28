package com.example.notecast.data.remote

import com.example.notecast.data.remote.mapping.toCreateRequest
import com.example.notecast.data.remote.mapping.toDomain
import com.example.notecast.data.remote.mapping.toUpdateRequest
import com.example.notecast.domain.model.Folder
import retrofit2.HttpException
import javax.inject.Inject

/**
 * Remote data source cho Folder, gọi NoteServicesAPI (các endpoint /folders).
 */
class FolderRemoteDataSource @Inject constructor(
    private val api: NoteServiceApi,
) {

    suspend fun fetchAllFolders(): List<Folder> =
        api.listFolders().map { it.toDomain() }

    suspend fun createFolder(folder: Folder): Folder =
        api.createFolder(folder.toCreateRequest()).let { resp ->
            Folder(
                id = resp.folder_id,
                name = resp.name,
                colorHex = resp.color_hex,
                createdAt = resp.created_at,
                updatedAt = resp.created_at,
                isSynced = true,
                isDeleted = false,
            )
        }

    suspend fun updateFolder(folder: Folder): Boolean =
        api.updateFolder(folder.id, folder.toUpdateRequest()).updated

    suspend fun deleteFolder(id: String): Boolean =
        api.deleteFolder(id).deleted


    /**
     * Optional: Lấy folder theo id từ NoteServices.
     * Dùng an toàn: nếu server trả 404 (folder không tồn tại), trả về null thay vì crash.
     */
    suspend fun safeGetFolderById(id: String): Folder? {
        return try {
            api.getFolderById(id).toDomain()
        } catch (e: HttpException) {
            if (e.code() == 404) {
                // Folder đã bị xoá hoặc không tồn tại: không crash, trả null để UI fallback.
                null
            } else {
                throw e
            }
        }
    }
}

