package com.example.notecast.domain.repository

import com.example.notecast.domain.model.Note
import kotlinx.coroutines.flow.Flow

interface NoteRepository {
    fun allNotes(): Flow<List<Note>>
    suspend fun getNoteById(id: String): Note?
    suspend fun insertNote(note: Note)
    suspend fun updateNote(note: Note)
    suspend fun deleteNote(id: String)
    suspend fun searchNotes(query: String): List<Note>
    suspend fun syncPending() // trigger sync for notes (or delegate to SyncUseCase)
}