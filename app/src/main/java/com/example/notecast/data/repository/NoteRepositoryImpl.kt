package com.example.notecast.data.repository

import com.example.notecast.data.local.dao.NoteDao
import com.example.notecast.data.local.mapper.EntityMapper
import com.example.notecast.domain.model.Note
import com.example.notecast.domain.repository.NoteRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation MỚI của NoteRepository.
 * Chỉ phụ thuộc vào 1 DAO duy nhất: [NoteDao].
 */
@Singleton
class NoteRepositoryImpl @Inject constructor(
    private val noteDao: NoteDao // <-- CHỈ INJECT 1 DAO
) : NoteRepository {

    /**
     * LẤY (READ): Sử dụng mapper [noteWithDetailsToDomain]
     */
    override fun getAllNotes(): Flow<List<Note>> {
        // 1. Gọi DAO, lấy về Flow<List<NoteWithDetails>> (POJO)
        return noteDao.getAllNotes().map { listNwd ->
            // 2. Map POJO -> Domain Model
            listNwd.map { nwd -> EntityMapper.noteWithDetailsToDomain(nwd) }
        }
    }

    override fun getNotesByFolder(folderId: String): Flow<List<Note>> {
        return noteDao.getNotesByFolder(folderId).map { listNwd ->
            listNwd.map { nwd -> EntityMapper.noteWithDetailsToDomain(nwd) }
        }
    }

    override fun getUncategorizedNotes(): Flow<List<Note>> {
        return noteDao.getUncategorizedNotes().map { listNwd ->
            listNwd.map { nwd -> EntityMapper.noteWithDetailsToDomain(nwd) }
        }
    }

    override fun getNoteById(id: String): Flow<Note?> {
        // 1. Gọi DAO, lấy về Flow<NoteWithDetails?> (POJO)
        return noteDao.getNoteWithDetails(id).map { nwd ->
            // 2. Map POJO -> Domain Model (nếu không null)
            nwd?.let { EntityMapper.noteWithDetailsToDomain(it) }
        }
    }

    /**
     * GHI (WRITE): Sử dụng các mapper "tách" (domainTo...Entity)
     */
    override suspend fun saveNote(note: Note) {
        // "Tách" (split) 1 Domain Model [Note] thành 4 Entities
        val noteEntity = EntityMapper.domainToNoteEntity(note).copy(isSynced = false)
        val audioEntity = EntityMapper.domainToAudioEntity(note)?.copy(isSynced = false)
        val transcriptEntity = EntityMapper.domainToTranscriptEntity(note)?.copy(isSynced = false)
        val processedEntity = EntityMapper.domainToProcessedTextEntity(note)?.copy(isSynced = false)

        // Ghi xuống CSDL
        noteDao.upsertNote(noteEntity)

        if (audioEntity != null) {
            noteDao.upsertAudio(audioEntity)
        }
        if (transcriptEntity != null) {
            noteDao.upsertTranscript(transcriptEntity)
        }
        if (processedEntity != null) {
            noteDao.upsertProcessedText(processedEntity)
        }
    }

    override suspend fun deleteNote(id: String) {
        // Dùng "xóa mềm"
        noteDao.softDeleteNote(id, System.currentTimeMillis())
    }

    override suspend fun searchNotes(query: String): List<Note> {
        // TODO: Cần implement logic tìm kiếm
        println("WARN: Search functionality is not yet implemented in DAO.")
        return emptyList()
    }

    override suspend fun syncPending() {
        // TODO: Implement sync logic
        println("Sync logic is skipped.")
    }
}