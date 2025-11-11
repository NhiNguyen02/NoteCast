package com.example.notecast.data.repository

import com.example.notecast.domain.model.Note
import com.example.notecast.domain.repository.NoteRepository
import javax.inject.Inject

// Giả định Note Model đã được chuyển vào domain/model/Note.kt
// (Hiện tại đang nằm trong ViewModel, sẽ chuyển sau)

/**
 * Implementation của NoteRepository, xử lý các nguồn dữ liệu (Firestore, Local DB).
 * Tạm thời, chúng ta sẽ giả lập dữ liệu.
 */
class NoteRepositoryImpl @Inject constructor(
    // @Inject internalDataSource: NoteLocalDataSource,
    // @Inject externalDataSource: NoteRemoteDataSource
) : NoteRepository {

    // Giả lập database
    private val mockDb = mutableMapOf<Int, Note>()

    override suspend fun getNoteById(noteId: Int): Note {
        // Trong thực tế: Gọi Local DB hoặc Remote API
        return mockDb[noteId] ?: throw NoSuchElementException("Note not found.")
    }

    override suspend fun saveNote(note: Note) {
        // Trong thực tế: Lưu vào Local DB / Firestore
        mockDb[note.id] = note
        println("Repository: Note saved successfully with ID ${note.id}")
    }
}