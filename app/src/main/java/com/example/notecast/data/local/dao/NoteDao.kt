package com.example.notecast.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.example.notecast.data.local.entities.AudioEntity
import com.example.notecast.data.local.entities.NoteEntity
import com.example.notecast.data.local.entities.NoteWithAudio
import kotlinx.coroutines.flow.Flow

@Dao
interface NoteDao {

    // --- CÁC HÀM UPSERT (INSERT/UPDATE) ---
    // Chúng ta gọi các hàm này từ Repository

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertNote(note: NoteEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAudio(audio: AudioEntity)

    // --- CÁC HÀM TRANSACTION (ĐẢM BẢO TOÀN VẸN DỮ LIỆU) ---

    /**
     * Dùng khi tạo Voice Note mới.
     * @Transaction đảm bảo cả 3 hành động thành công, hoặc thất bại cùng nhau.
     */
    @Transaction
    suspend fun createVoiceNote(
        note: NoteEntity,
        audio: AudioEntity,
    ) {
        upsertNote(note)
        upsertAudio(audio)
    }

    // --- CÁC HÀM GET (ĐỌC DỮ LIỆU) ---
    // Các hàm này dùng POJO NoteWithDetails

    /**
     * Lấy chi tiết 1 ghi chú (dùng POJO NoteWithDetails).
     * @Transaction là bắt buộc khi dùng @Relation.
     */
    @Transaction
    @Query("SELECT * FROM note WHERE id = :noteId AND isDeleted = 0")
    fun getNoteWithAudio(noteId: String): Flow<NoteWithAudio?>

    /**
     * Lấy tất cả ghi chú (cho màn hình chính).
     * Sắp xếp: Ghim lên đầu, sau đó theo cập nhật mới nhất.
     */
    @Transaction
    @Query("SELECT * FROM note WHERE isDeleted = 0 ORDER BY updatedAt DESC")
    fun getAllNotesWithAudio(): Flow<List<NoteWithAudio>>

    /**
     * Lấy các ghi chú theo thư mục.
     */
    @Transaction
    @Query("SELECT * FROM note WHERE folderId = :folderId AND isDeleted = 0 ORDER BY updatedAt DESC")
    fun getNotesWithAudioByFolder(folderId: String): Flow<List<NoteWithAudio>>

    /**
     * Lấy các ghi chú "Chưa phân loại" (folderId = null).
     */
    @Transaction
    @Query("SELECT * FROM note WHERE folderId IS NULL AND isDeleted = 0 ORDER BY updatedAt DESC")
    fun getUncategorizedNotesWithAudio(): Flow<List<NoteWithAudio>>

    // --- CÁC HÀM XÓA VÀ ĐỒNG BỘ ---

    /**
     * "Xóa mềm" một ghi chú (giống FolderDao).
     */
    @Query("UPDATE note SET isDeleted = 1, updatedAt = :timestamp, isSynced = 0 WHERE id = :noteId")
    suspend fun softDeleteNote(noteId: String, timestamp: Long)

    // --- Lấy dữ liệu chưa đồng bộ (cho SyncUseCase) ---

    @Query("SELECT * FROM note WHERE isSynced = 0")
    suspend fun getUnsyncedNotes(): List<NoteEntity>


    @Query("UPDATE note SET isSynced = 1 WHERE id = :noteId")
    suspend fun markNoteSynced(noteId: String)

}