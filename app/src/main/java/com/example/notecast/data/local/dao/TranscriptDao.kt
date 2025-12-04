package com.example.notecast.data.local.dao


import androidx.room.*
import com.example.notecast.data.local.entities.TranscriptEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TranscriptDao {
    @Query("SELECT * FROM transcript WHERE audioId = :audioId ORDER BY createdAt DESC")
    fun transcriptsForAudio(audioId: String): Flow<List<TranscriptEntity>>

    @Query("SELECT * FROM transcript WHERE id = :id LIMIT 1")
    suspend fun getById(id: String): TranscriptEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(transcript: TranscriptEntity)

    @Update
    suspend fun update(transcript: TranscriptEntity)

    @Query("SELECT * FROM transcript WHERE isSynced = 0 AND isDeleted = 0")
    suspend fun unsyncedTranscripts(): List<TranscriptEntity>
}