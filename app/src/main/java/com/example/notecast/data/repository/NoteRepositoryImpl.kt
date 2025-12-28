package com.example.notecast.data.repository

import android.util.Log
import com.example.notecast.data.local.dao.NoteDao
import com.example.notecast.data.local.entities.AudioEntity
import com.example.notecast.data.local.mapper.MappingEntityToDomain
import com.example.notecast.domain.model.NoteDomain
import com.example.notecast.domain.repository.NoteRemoteRepository
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
    private val noteDao: NoteDao, // <-- CHỈ INJECT 1 DAO
    private val noteRemoteRepository: NoteRemoteRepository,
) : NoteRepository {

    /**
     * LẤY (READ): sử dụng mapper MappingEntityToDomain.noteWithAudioToDomain
     */
    override fun getAllNotes(): Flow<List<NoteDomain>> {
        return noteDao.getAllNotesWithAudio().map { list ->
            list.map { MappingEntityToDomain.noteWithAudioToDomain(it) }
        }
    }

    override fun getNotesByFolder(folderId: String): Flow<List<NoteDomain>> {
        return noteDao.getNotesWithAudioByFolder(folderId).map { list ->
            list.map { MappingEntityToDomain.noteWithAudioToDomain(it) }
        }
    }

    override fun getUncategorizedNotes(): Flow<List<NoteDomain>> {
        return noteDao.getUncategorizedNotesWithAudio().map { list ->
            list.map { MappingEntityToDomain.noteWithAudioToDomain(it) }
        }
    }

    override fun getNoteById(id: String): Flow<NoteDomain?> {
        return noteDao.getNoteWithAudio(id).map { nwa ->
            nwa?.let { MappingEntityToDomain.noteWithAudioToDomain(it) }
        }
    }

    override suspend fun saveNote(note: NoteDomain) {
        val noteEntity = MappingEntityToDomain.domainToNoteEntity(note)
        val audioEntity: AudioEntity? = MappingEntityToDomain.domainToAudioEntity(note)
        noteDao.upsertNote(noteEntity)
        audioEntity?.let { noteDao.upsertAudio(it) }
    }

    override suspend fun deleteNote(id: String) {
        noteDao.softDeleteNote(id, System.currentTimeMillis())
    }

    override suspend fun searchNotes(query: String): List<NoteDomain> {
        return emptyList()
    }

    override suspend fun syncPending() {
        // Backend NoteServices là source of truth.
        // Thay vì đẩy local notes lên server, ta kéo remote notes
        // và merge vào Room.
        val remoteNotes: List<NoteDomain> = noteRemoteRepository.fetchAllNotes()
        Log.d("SyncNotes", "Fetched ${remoteNotes.size} notes from remote")
        for (remote in remoteNotes) {
            Log.d(
                "SyncNotes",
                "Saving remote note id=${remote.id}, title=${remote.title}, type=${remote.type}, folderId=${remote.folderId}"
            )
            saveNote(remote)
            noteDao.markNoteSynced(remote.id)
        }
        // Chính sách xử lý note local không còn trên server (xóa/giữ) có thể bổ sung sau.
    }

    override suspend fun markSynced(noteId: String) {
        noteDao.markNoteSynced(noteId)
    }
}