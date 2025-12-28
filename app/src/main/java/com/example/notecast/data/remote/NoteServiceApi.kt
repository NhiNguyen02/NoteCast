package com.example.notecast.data.remote

import com.example.notecast.data.remote.dto.GenerateRequest
import com.example.notecast.data.remote.dto.NoteAudioCreateRequest
import com.example.notecast.data.remote.dto.NoteCreateResponse
import com.example.notecast.data.remote.dto.NoteDto
import com.example.notecast.data.remote.dto.NoteTextCreateRequest
import com.example.notecast.data.remote.dto.NoteTextInternalCreateRequest
import com.example.notecast.data.remote.dto.NoteUpdateRequest
import com.example.notecast.data.remote.dto.NoteUpdateResponse
import com.example.notecast.data.remote.dto.RegenerateResponse
import com.example.notecast.data.remote.dto.FolderCreateRequest
import com.example.notecast.data.remote.dto.FolderCreateResponse
import com.example.notecast.data.remote.dto.FolderDto
import com.example.notecast.data.remote.dto.FolderUpdateRequest
import com.example.notecast.data.remote.dto.FolderUpdateResponse
import com.example.notecast.data.remote.dto.FolderDeleteResponse
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * Retrofit client cho NoteService (backend quản lý note + folders + post-processing AI),
 * align 100% với tài liệu `noteservices_api_endpoints.md` + internal endpoints.
 */
interface NoteServiceApi {

    // Notes ---

    // 1) Create text note: POST /notes/text
    @POST("notes/text")
    suspend fun createTextNote(
        @Body body: NoteTextCreateRequest,
    ): NoteCreateResponse

    // 1b) Create text note (internal, id do client sinh): POST /internal/notes/text
    @POST("internal/notes/text")
    suspend fun createTextNoteInternal(
        @Body body: NoteTextInternalCreateRequest,
    ): NoteCreateResponse

    // 2) Create audio note (internal): POST /internal/notes/audio
    @POST("internal/notes/audio")
    suspend fun createAudioNote(
        @Body body: NoteAudioCreateRequest,
    ): NoteCreateResponse

    // 3) Regenerate / enrichment: POST /notes/{note_id}/regenerate
    @POST("notes/{id}/regenerate")
    suspend fun regenerate(
        @Path("id") noteId: String,
        @Body body: GenerateRequest,
    ): RegenerateResponse

    // 4) Get note by id: GET /notes/{note_id}
    @GET("notes/{id}")
    suspend fun getNote(
        @Path("id") noteId: String,
    ): NoteDto

    // 5) List notes: GET /notes?folder_id=...
    @GET("notes")
    suspend fun listNotes(
        @Query("folder_id") folderId: String? = null,
    ): List<NoteDto>

    // 6) Update note (title / folder): PATCH /notes/{note_id}
    @PATCH("notes/{id}")
    suspend fun updateNote(
        @Path("id") noteId: String,
        @Body body: NoteUpdateRequest,
    ): NoteUpdateResponse

    // Folders ---

    // 7) Create folder: POST /folders
    @POST("folders")
    suspend fun createFolder(
        @Body body: FolderCreateRequest,
    ): FolderCreateResponse

    // 8) List folders: GET /folders
    @GET("folders")
    suspend fun listFolders(): List<FolderDto>

    // 9) Get folder by id: GET /folders/{folder_id}
    @GET("folders/{id}")
    suspend fun getFolderById(
        @Path("id") folderId: String,
    ): FolderDto

    // 10) Update folder: PATCH /folders/{folder_id}
    @PATCH("folders/{id}")
    suspend fun updateFolder(
        @Path("id") folderId: String,
        @Body body: FolderUpdateRequest,
    ): FolderUpdateResponse

    // 11) Delete folder: DELETE /folders/{folder_id}
    @DELETE("folders/{id}")
    suspend fun deleteFolder(
        @Path("id") folderId: String,
    ): FolderDeleteResponse

    // SSE /notes/{id}/events nếu cần: dùng client riêng (không khai báo ở đây)
}
