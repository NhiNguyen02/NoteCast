package com.example.notecast.domain.repository

import com.example.notecast.domain.model.NoteDomain
import kotlinx.coroutines.flow.Flow

/**
 * Interface cho Domain Layer.
 * Quản lý TẤT CẢ logic ghi chú (bao gồm audio...).
 */
interface NoteRepository {
    // === ĐỌC (READ) ===
    fun getAllNotes(): Flow<List<NoteDomain>>
    fun getNotesByFolder(folderId: String): Flow<List<NoteDomain>>
    fun getUncategorizedNotes(): Flow<List<NoteDomain>>
    fun getNoteById(id: String): Flow<NoteDomain?>

    // === GHI (WRITE) ===
    suspend fun saveNote(note: NoteDomain)
    suspend fun deleteNote(id: String)

    // === KHÁC ===
    suspend fun syncPending()
    suspend fun searchNotes(query: String): List<NoteDomain>
    suspend fun markSynced(noteId: String)
}