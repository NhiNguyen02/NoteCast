package com.example.notecast.data.remote.mapping

import com.example.notecast.data.remote.dto.AudioMetadataDto
import com.example.notecast.data.remote.dto.ChunkDto
import com.example.notecast.data.remote.dto.MindmapDto
import com.example.notecast.data.remote.dto.NoteAudioCreateRequest
import com.example.notecast.data.remote.dto.NoteDto
import com.example.notecast.data.remote.dto.NoteMetadataDto
import com.example.notecast.data.remote.dto.NoteTextCreateRequest
import com.example.notecast.data.remote.dto.NoteTextInternalCreateRequest
import com.example.notecast.domain.model.AudioChunk
import com.example.notecast.domain.model.AudioDomain
import com.example.notecast.domain.model.MindMapNode
import com.example.notecast.domain.model.NoteDomain
import com.example.notecast.domain.model.NoteStatus
import com.example.notecast.domain.model.NoteType
import kotlinx.serialization.json.Json

// NoteDto → NoteDomain (API → Domain)
fun NoteDto.toDomain(): NoteDomain {
    val audioMeta = metadata?.audio
    val audioDomain = if (audioMeta != null && audioMeta.duration != null && audioMeta.sampleRate != null) {
        // Sử dụng audio URL từ backend (metadata.audio.url)
        // Fallback: construct URL từ note_id nếu backend chưa có
        val audioUrl = audioMeta.url?.takeIf { it.isNotBlank() }
            ?: "https://bichnhan2701-noteservicesapi.hf.space/notes/$note_id/audio"

        AudioDomain(
            durationSec = audioMeta.duration,
            sampleRate = audioMeta.sampleRate,
            chunks = (audioMeta.chunks ?: emptyList()).map { AudioChunk(it.start, it.end, it.text) },
            localFilePath = null,  // Client không có local file khi sync từ backend
            cloudUrl = audioUrl,   // URL để stream/download audio
        )
    } else null

    val coarseStatus = when (status) {
        "created" -> NoteStatus.CREATED
        "processing", "normalize_done", "keywords_done", "summary_done", "mindmap_done" -> NoteStatus.PROCESSING
        "ready" -> NoteStatus.READY
        "error" -> NoteStatus.ERROR
        else -> NoteStatus.CREATED
    }

    return NoteDomain(
        id = note_id,
        type = when (type) {
            "audio" -> NoteType.AUDIO
            "text" -> NoteType.TEXT
            else -> if (audioDomain != null) NoteType.AUDIO else NoteType.TEXT
        },
        title = title,
        rawText = raw_text,
        normalizedText = normalized_text,
        keywords = keywords ?: emptyList(),
        summary = summary,
        // Serialize full MindMapNode tree to JSON string in domain
        mindmapJson = rootToJson(mindmap?.root),
        audio = audioDomain,
        folderId = folder_id,
        status = coarseStatus,
        statusRaw = status,
        isFavorite = false,
        isPinned = false,
        createdAt = created_at ?: 0L,
        updatedAt = updated_at ?: 0L,
    )
}

// Domain → NoteDto (đồng bộ lại Firestore / backend NoteServices)
fun NoteDomain.toDto(mindmapRoot: MindMapNode?): NoteDto =
    NoteDto(
        note_id = id,
        type = when (type) {
            NoteType.AUDIO -> "audio"
            NoteType.TEXT -> "text"
        },
        title = title,
        raw_text = rawText,
        normalized_text = normalizedText,
        keywords = keywords,
        summary = summary,
        mindmap = MindmapDto(root = mindmapRoot),
        folder_id = folderId,
        metadata = audio?.let { audioDomain ->
            NoteMetadataDto(
                audio = AudioMetadataDto(
                    duration = audioDomain.durationSec,
                    sampleRate = audioDomain.sampleRate,
                    chunks = audioDomain.chunks.map {
                        ChunkDto(start = it.start, end = it.end, text = it.text)
                    },
                    asrModel = null, // có thể map từ AudioEntity.asrModel nếu cần
                )
            )
        },
        status = when (status) {
            NoteStatus.CREATED -> "created"
            NoteStatus.PROCESSING -> "processing"
            NoteStatus.READY -> "ready"
            NoteStatus.ERROR -> "error"
        },
        created_at = createdAt,
        updated_at = updatedAt,
    )

// Domain → API create requests
// 1) Text note: dùng cho POST /notes/text
fun NoteDomain.toTextCreateRequest(generateTasks: List<String> = emptyList()): NoteTextCreateRequest =
    NoteTextCreateRequest(
        raw_text = requireNotNull(rawText) { "Text note must have rawText when creating" },
        folder_id = folderId,
        generate = generateTasks,
    )

// 1b) Text note (internal, id do client sinh): dùng cho POST /internal/notes/text
fun NoteDomain.toTextInternalCreateRequest(generateTasks: List<String> = emptyList()): NoteTextInternalCreateRequest =
    NoteTextInternalCreateRequest(
        note_id = id,
        raw_text = requireNotNull(rawText) { "Text note must have rawText when creating (internal)" },
        folder_id = folderId,
        generate = generateTasks,
    )

// 2) Audio note (internal): dùng cho POST /internal/notes/audio
fun NoteDomain.toAudioCreateRequest(
    asrModel: String? = null,
    generateTasks: List<String> = emptyList(),
): NoteAudioCreateRequest {
    val audioDomain = requireNotNull(audio) { "Audio note must have audio metadata when creating" }
    val audioMetadata = AudioMetadataDto(
        duration = audioDomain.durationSec,
        sampleRate = audioDomain.sampleRate,
        chunks = audioDomain.chunks.map { ChunkDto(it.start, it.end, it.text) },
        asrModel = asrModel,
    )
    return NoteAudioCreateRequest(
        note_id = id,
        raw_text = requireNotNull(rawText) { "Audio note must have rawText (transcript) when creating" },
        metadata = NoteMetadataDto(audio = audioMetadata),
        generate = generateTasks,
    )
}

// Domain helper: parse MindMapNode từ mindmapJson trong Domain
fun NoteDomain.parseMindmapRoot(): MindMapNode? =
    mindmapJson?.let { json ->
        runCatching { noteJson.decodeFromString<MindMapNode>(json) }.getOrNull()
    }

// Helper để serialize MindMapNode → JSON
private val noteJson = Json { ignoreUnknownKeys = true; encodeDefaults = true }
private fun rootToJson(root: MindMapNode?): String? = root?.let { noteJson.encodeToString(it) }
