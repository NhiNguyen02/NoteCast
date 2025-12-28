package com.example.notecast.domain.repository

import com.example.notecast.domain.model.NoteDomain
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow

/**
 * Remote gateway cho NoteServices API.
 *
 * - createNote: push một NoteDomain lên backend (text hoặc audio) với danh sách task generate.
 * - regenerate: gọi POST /notes/{note_id}/regenerate.
 * - fetchNote: lấy NoteDomain (map từ NoteDto) trực tiếp từ NoteServices (GET /notes/{note_id}).
 */
interface NoteRemoteRepository {
    suspend fun createNote(note: NoteDomain, generateTasks: List<String>)
    suspend fun regenerate(noteId: String, generateTasks: List<String>)
    suspend fun fetchNote(noteId: String): NoteDomain?

    // New: fetch all notes from remote for pull-based sync
    suspend fun fetchAllNotes(): List<NoteDomain>

    @OptIn(ExperimentalCoroutinesApi::class)
    suspend fun observeRemoteNote(noteId: String): Flow<NoteDomain>
}
