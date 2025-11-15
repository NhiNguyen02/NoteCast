package com.example.notecast.data.local.dao


import androidx.room.*
import com.example.notecast.data.local.entities.AudioEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AudioDao {
    @Query("SELECT * FROM audio WHERE isDeleted = 0 ORDER BY createdAt DESC")
    fun allAudio(): Flow<List<AudioEntity>>

    @Query("SELECT * FROM audio WHERE id = :id LIMIT 1")
    suspend fun getById(id: String): AudioEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(audio: AudioEntity)

    @Update
    suspend fun update(audio: AudioEntity)

    @Query("SELECT * FROM audio WHERE isSynced = 0 AND isDeleted = 0")
    suspend fun unsyncedAudio(): List<AudioEntity>

    @Query("SELECT * FROM audio WHERE isSynced = 0 AND isDeleted = 1")
    suspend fun unsyncedDeletedAudio(): List<AudioEntity>
}