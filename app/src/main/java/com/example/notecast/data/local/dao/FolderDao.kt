package com.example.notecast.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.notecast.data.local.entities.FolderEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FolderDao {

    /**
     * Dùng để Thêm mới hoặc Cập nhật (nếu đã tồn tại) một thư mục.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertFolder(folder: FolderEntity)

    /**
     * "Xóa mềm" một thư mục (quan trọng cho việc đồng bộ).
     */
    @Query("UPDATE folder SET isDeleted = 1, updatedAt = :timestamp, isSynced = 0 WHERE id = :folderId")
    suspend fun softDeleteFolder(folderId: String, timestamp: Long)

    /**
     * Lấy một thư mục theo ID.
     */
    @Query("SELECT * FROM folder WHERE id = :folderId AND isDeleted = 0")
    fun getFolderById(folderId: String): Flow<FolderEntity?>

    /**
     * Lấy tất cả thư mục (để hiển thị danh sách).
     */
    @Query("SELECT * FROM folder WHERE isDeleted = 0 ORDER BY updatedAt DESC")
    fun getAllFolders(): Flow<List<FolderEntity>>

    /**
     * Lấy các thư mục chưa đồng bộ (dùng cho SyncUseCase).
     */
    @Query("SELECT * FROM folder WHERE isSynced = 0")
    suspend fun getUnsyncedFolders(): List<FolderEntity>
}