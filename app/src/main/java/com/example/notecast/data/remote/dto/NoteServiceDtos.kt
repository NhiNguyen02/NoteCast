package com.example.notecast.data.remote.dto

import com.example.notecast.domain.model.MindMapNode
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// --- Core Note DTO (backend source-of-truth) ---
@Serializable
data class NoteDto(
    @SerialName("note_id")
    val note_id: String,
    @SerialName("type")
    val type: String? = null, // "audio" | "text"
    @SerialName("title")
    val title: String? = null,
    @SerialName("raw_text")
    val raw_text: String? = null,
    @SerialName("normalized_text")
    val normalized_text: String? = null,
    @SerialName("keywords")
    val keywords: List<String>? = null,
    @SerialName("summary")
    val summary: String? = null,
    @SerialName("mindmap")
    val mindmap: MindmapDto? = null,
    @SerialName("folder_id")
    val folder_id: String? = null,
    @SerialName("metadata")
    val metadata: NoteMetadataDto? = null,
    @SerialName("status")
    val status: String? = null,
    @SerialName("created_at")
    val created_at: Long? = null,
    @SerialName("updated_at")
    val updated_at: Long? = null,
)

@Serializable
data class NoteMetadataDto(
    @SerialName("audio")
    val audio: AudioMetadataDto? = null,
)

@Serializable
data class AudioMetadataDto(
    @SerialName("duration")
    val duration: Double? = null,
    @SerialName("sample_rate")
    val sampleRate: Int? = null,
    @SerialName("chunks")
    val chunks: List<ChunkDto>? = null,
    @SerialName("asr_model")
    val asrModel: String? = null,
    @SerialName("url")
    val url: String? = null,  // Audio file URL từ backend
)

@Serializable
data class ChunkDto(
    @SerialName("start")
    val start: Double,
    @SerialName("end")
    val end: Double,
    @SerialName("text")
    val text: String,
)

// Mindmap payload, map 1-1 với backend: { "mindmap": { "root": { ...MindMapNode } } }
@Serializable
data class MindmapDto(
    @SerialName("root")
    val root: MindMapNode? = null,
)

// --- Create / update / regenerate DTOs align 100% với NoteServices API ---

// 1) Create text note: POST /notes/text
@Serializable
data class NoteTextCreateRequest(
    @SerialName("raw_text")
    val raw_text: String,
    @SerialName("folder_id")
    val folder_id: String? = null,
    @SerialName("generate")
    val generate: List<String> = emptyList(), // ["normalize","keywords","summary","mindmap"]
)

// 2) Create audio note (internal): POST /internal/notes/audio
@Serializable
data class NoteAudioCreateRequest(
    @SerialName("note_id")
    val note_id: String,
    @SerialName("raw_text")
    val raw_text: String,
    @SerialName("metadata")
    val metadata: NoteMetadataDto,
    @SerialName("generate")
    val generate: List<String> = emptyList(),
)

// Shared small response: { "note_id": "...", "status": "created|processing|ready|error" }
@Serializable
data class NoteCreateResponse(
    @SerialName("note_id")
    val note_id: String,
    @SerialName("status")
    val status: String,
)

// 3) Regenerate / enrichment: POST /notes/{note_id}/regenerate
@Serializable
data class GenerateRequest(
    @SerialName("generate")
    val generate: List<String>, // required, must be subset of ["normalize","keywords","summary","mindmap"]
)

@Serializable
data class RegenerateResponse(
    @SerialName("note_id")
    val note_id: String,
    @SerialName("status")
    val status: String, // "processing" expected
)

// 4) PATCH /notes/{note_id}
@Serializable
data class NoteUpdateRequest(
    @SerialName("folder_id")
    val folder_id: String? = null,
    @SerialName("title")
    val title: String? = null,
)

@Serializable
data class NoteUpdateResponse(
    @SerialName("note_id")
    val note_id: String,
    @SerialName("updated")
    val updated: Boolean,
)

// 5) Create text note (internal): POST /internal/notes/text
//    Dùng khi client chủ động cung cấp note_id (id do client sinh),
//    tương tự /internal/notes/audio.
@Serializable
data class NoteTextInternalCreateRequest(
    @SerialName("note_id")
    val note_id: String,
    @SerialName("raw_text")
    val raw_text: String,
    @SerialName("folder_id")
    val folder_id: String? = null,
    @SerialName("generate")
    val generate: List<String> = emptyList(),
)
