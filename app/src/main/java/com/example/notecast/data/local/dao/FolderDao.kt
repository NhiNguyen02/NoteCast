package com.example.notecast.data.local.dao

import androidx.room.*
import com.example.notecast.data.local.entities.FolderEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FolderDao {
    @Query("SELECT * FROM folder WHERE isDeleted = 0 ORDER BY updatedAt DESC")
    fun allFolders(): Flow<List<FolderEntity>>

    @Query("SELECT * FROM folder WHERE id = :id LIMIT 1")
    suspend fun getById(id: String): FolderEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(folder: FolderEntity)

    @Update
    suspend fun update(folder: FolderEntity)

    @Delete
    suspend fun delete(folder: FolderEntity)
}