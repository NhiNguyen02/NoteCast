package com.example.notecast.data.local.dao


import androidx.room.*
import com.example.notecast.data.local.entities.NoteEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface NoteDao {
    @Query("SELECT * FROM note WHERE isDeleted = 0 ORDER BY updatedAt DESC")
    fun allNotes(): Flow<List<NoteEntity>>

    @Query("SELECT * FROM note WHERE id = :id LIMIT 1")
    suspend fun getById(id: String): NoteEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(note: NoteEntity)

    @Update
    suspend fun update(note: NoteEntity)

    @Query("SELECT * FROM note WHERE isSynced = 0 AND isDeleted = 0")
    suspend fun unsyncedNotes(): List<NoteEntity>

    @Query("SELECT * FROM note WHERE isSynced = 0 AND isDeleted = 1")
    suspend fun unsyncedDeletedNotes(): List<NoteEntity>
}