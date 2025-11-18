package com.example.notecast.domain.repository

import com.example.notecast.domain.model.Note
import kotlinx.coroutines.flow.Flow

/**
 * Interface (Hợp đồng) MỚI cho Domain Layer.
 * Quản lý TẤT CẢ logic ghi chú (bao gồm audio, transcript...).
 */
interface NoteRepository {
    // === ĐỌC (READ) ===
    fun getAllNotes(): Flow<List<Note>>
    fun getNotesByFolder(folderId: String): Flow<List<Note>>
    fun getUncategorizedNotes(): Flow<List<Note>>
    fun getNoteById(id: String): Flow<Note?> // Trả về Flow

    // === GHI (WRITE) ===
    suspend fun saveNote(note: Note)
    suspend fun deleteNote(id: String) // Xóa mềm

    // === CÁC HÀM KHÁC ===
    suspend fun syncPending()
    suspend fun searchNotes(query: String): List<Note>
}