package com.example.notecast.data.repository

import android.util.Log
import com.example.notecast.data.remote.NoteEventsSseClient
import com.example.notecast.data.remote.NoteServiceApi
import com.example.notecast.data.remote.dto.GenerateRequest
import com.example.notecast.data.remote.mapping.toAudioCreateRequest
import com.example.notecast.data.remote.mapping.toDomain
import com.example.notecast.data.remote.mapping.toTextCreateRequest
import com.example.notecast.data.remote.mapping.toTextInternalCreateRequest
import com.example.notecast.domain.model.NoteDomain
import com.example.notecast.domain.model.NoteType
import com.example.notecast.domain.repository.NoteRemoteRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

/**
 * Triển khai NoteRemoteRepository sử dụng Retrofit NoteServiceApi.
 */
class RemoteNoteServiceRepositoryImpl @Inject constructor(
    private val api: NoteServiceApi,
    private val eventsClient: NoteEventsSseClient,
) : NoteRemoteRepository {

    override suspend fun createNote(note: NoteDomain, generateTasks: List<String>) {
        // NOTE:
        // - Voice notes created via PhoWhisper SHOULD NOT call this directly;
        //   backend already owns note_id and creates/updates the note.
        // - For text notes, we prefer the internal endpoint so that
        //   note_id is provided by client and stays consistent between
        //   local DB and backend.
        when (note.type) {
            NoteType.TEXT -> {
                // Nếu backend đã hỗ trợ /internal/notes/text, ưu tiên dùng nó
                val body = note.toTextInternalCreateRequest(generateTasks)
                Log.d("RemoteNoteRepo", "Creating text note on backend: note_id=${body.note_id}, generate=${body.generate}, rawText length=${body.raw_text.length}")
                try {
                    api.createTextNoteInternal(body)
                    Log.d("RemoteNoteRepo", "Successfully created text note: ${body.note_id}")
                } catch (e: Exception) {
                    Log.e("RemoteNoteRepo", "Failed to create text note: ${e.message}", e)
                    throw e
                }
            }
            NoteType.AUDIO -> {
                val body = note.toAudioCreateRequest(
                    asrModel = note.audio?.let { " " },
                    generateTasks = generateTasks
                )
                Log.d("RemoteNoteRepo", "Creating audio note on backend: note_id=${body.note_id}")
                api.createAudioNote(body)
            }
        }
    }

    override suspend fun fetchNote(noteId: String): NoteDomain {
        val dto = api.getNote(noteId)
        return dto.toDomain()
    }

    override suspend fun fetchAllNotes(): List<NoteDomain> {
        val dtos = api.listNotes(folderId = null)
        return dtos.map { it.toDomain() }
    }

    override suspend fun regenerate(noteId: String, generateTasks: List<String>) {
        api.regenerate(
            noteId = noteId,
            body = GenerateRequest(generate = generateTasks),
        )
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override suspend fun observeRemoteNote(noteId: String): Flow<NoteDomain> {
        return eventsClient.subscribeNoteEvents(noteId)
            .flatMapLatest {
                flow {
                    val dto = api.getNote(noteId)
                    emit(dto.toDomain())
                }
            }
    }
}
