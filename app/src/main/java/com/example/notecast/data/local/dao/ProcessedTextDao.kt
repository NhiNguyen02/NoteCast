package com.example.notecast.data.local.dao

import androidx.room.*
import com.example.notecast.data.local.entities.ProcessedTextEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ProcessedTextDao {
    @Query("SELECT * FROM processed_text WHERE transcriptId = :transcriptId")
    fun forTranscript(transcriptId: String): Flow<List<ProcessedTextEntity>>

    @Query("SELECT * FROM processed_text WHERE id = :id LIMIT 1")
    suspend fun getById(id: String): ProcessedTextEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(processed: ProcessedTextEntity)

    @Update
    suspend fun update(processed: ProcessedTextEntity)

    @Query("SELECT * FROM processed_text WHERE isSynced = 0 AND isDeleted = 0")
    suspend fun unsyncedProcessedText(): List<ProcessedTextEntity>
}