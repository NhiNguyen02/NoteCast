package com.example.notecast.data.repository

import com.example.notecast.data.local.dao.FolderDao
import com.example.notecast.data.local.dao.NoteDao
import com.example.notecast.data.local.dao.ProcessedTextDao
// SỬA: Import mapper
import com.example.notecast.data.local.mapper.EntityMapper
import com.example.notecast.domain.model.Note
import com.example.notecast.domain.repository.NoteRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.Date
import java.util.UUID
import javax.inject.Inject

/**
 * Implementation của NoteRepository.
 * Chỉ sử dụng Local DAOs (Room) theo yêu cầu.
 */
class NoteRepositoryImpl @Inject constructor(
    private val noteDao: NoteDao,
    private val folderDao: FolderDao,
    private val processedTextDao: ProcessedTextDao
    // Đã bỏ qua Firebase/Firestore
) : NoteRepository {

    override fun allNotes(): Flow<List<Note>> {
        return noteDao.allNotes().map { entityList ->
            entityList.map { entity ->
                // TODO: Cần logic truy vấn FolderDao
                EntityMapper.noteEntityToDomain(entity)
            }
        }
    }

    override suspend fun getNoteById(id: String): Note? {
        val entity = noteDao.getById(id)
        // TODO: Tải thư mục
        return entity?.let { EntityMapper.noteEntityToDomain(it) }
    }

    override suspend fun insertNote(note: Note) {
        val entity = EntityMapper.domainToNoteEntity(note).copy(
            id = UUID.randomUUID().toString(), // Đảm bảo ID mới
            isSynced = false,
            isDeleted = false,
            updatedAt = Date().time
        )
        noteDao.insert(entity)
    }

    override suspend fun updateNote(note: Note) {
        val existingEntity = noteDao.getById(note.id) ?: throw NoSuchElementException("Note không tồn tại để cập nhật")

        val entity = EntityMapper.domainToNoteEntity(note).copy(
            isSynced = false, // Đánh dấu cần đồng bộ
            updatedAt = Date().time,
            // Giữ nguyên các trường không có trong Model
            isDeleted = existingEntity.isDeleted
        )
        noteDao.update(entity)
    }

    override suspend fun deleteNote(id: String) {
        val entity = noteDao.getById(id)
        if (entity != null) {
            val deletedEntity = entity.copy(
                isDeleted = true,
                isSynced = false, // Đánh dấu cần đồng bộ (xóa)
                updatedAt = Date().time
            )
            noteDao.update(deletedEntity)
        }
    }

    override suspend fun searchNotes(query: String): List<Note> {
        // Cần thêm @Query hỗ trợ FTS hoặc LIKE trong NoteDao
        println("WARN: Search functionality is not yet implemented in DAO.")
        return emptyList()
    }

    override suspend fun syncPending() {
        // Người dùng yêu cầu "chưa dùng firebase"
        println("Sync logic is skipped (Firebase not included).")
    }
}