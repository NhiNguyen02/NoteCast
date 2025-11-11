package com.example.notecast.domain.repository

import com.example.notecast.domain.model.Note

interface NoteRepository {
    suspend fun getNoteById(noteId: Int): Note
    suspend fun saveNote(note: Note)
}