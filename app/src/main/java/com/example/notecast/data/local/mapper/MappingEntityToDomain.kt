package com.example.notecast.data.local.mapper

import com.example.notecast.data.local.entities.AudioEntity
import com.example.notecast.data.local.entities.FolderEntity
import com.example.notecast.data.local.entities.NoteEntity
import com.example.notecast.data.local.entities.NoteWithAudio
import com.example.notecast.domain.model.AudioChunk
import com.example.notecast.domain.model.AudioDomain
import com.example.notecast.domain.model.Folder
import com.example.notecast.domain.model.NoteDomain
import com.example.notecast.domain.model.NoteStatus
import com.example.notecast.domain.model.NoteType
import kotlinx.serialization.json.Json

/**
 * Mapper giữa Room Entities (Data Layer) và Domain Models (Domain Layer).
 * Được viết lại để hỗ trợ cấu trúc "hub-and-spoke" (NoteWithDetails).
 */
object MappingEntityToDomain {

    private val json = Json {
        encodeDefaults = true
        ignoreUnknownKeys = true
        prettyPrint = false
    }

    // --- Folder Mapping (1-1) ---

    fun folderEntityToDomain(e: FolderEntity): Folder = Folder(
        id = e.id,
        name = e.name,
        colorHex = e.colorHex,
        createdAt = e.createdAt,
        updatedAt = e.updatedAt,
        isSynced = e.isSynced,
        isDeleted = e.isDeleted
    )

    fun domainToFolderEntity(d: Folder): FolderEntity = FolderEntity(
        id = d.id,
        name = d.name,
        colorHex = d.colorHex,
        createdAt = d.createdAt,
        updatedAt = d.updatedAt,
        isSynced = d.isSynced,
        isDeleted = d.isDeleted
    )
//Room → Domain (NoteWithAudio → NoteDomain)
    /**
     * Mapping mới: NoteWithAudio (Room) -> NoteDomain (Domain)
     */
    fun noteWithAudioToDomain(nwa: NoteWithAudio): NoteDomain {
        val note = nwa.note
        val audio = nwa.audio
        val keywords: List<String> = try {
            note.keywordsJson?.takeIf { it.isNotBlank() }?.let { json.decodeFromString(it) } ?: emptyList()
        } catch (_: Exception) { emptyList() }

        val audioDomain = audio?.let {
            AudioDomain(
                durationSec = it.durationSec,
                sampleRate = it.sampleRate,
                chunks = try {
                    it.chunksJson?.takeIf { cj -> cj.isNotBlank() }?.let { cj ->
                        json.decodeFromString<List<AudioChunk>>(cj)
                    } ?: emptyList()
                } catch (_: Exception) { emptyList() },
                localFilePath = it.localFilePath,
                cloudUrl = it.cloudUrl,
            )
        }

        return NoteDomain(
            id = note.id,
            type = when (note.type) {
                "audio" -> NoteType.AUDIO
                "text" -> NoteType.TEXT
                else -> if (audioDomain != null) NoteType.AUDIO else NoteType.TEXT
            },
            title = note.title,
            rawText = note.rawText,
            normalizedText = note.normalizedText,
            keywords = keywords,
            summary = note.summary,
            mindmapJson = note.mindmapJson,
            audio = audioDomain,
            folderId = note.folderId,
            status = when (note.status) {
                "created" -> NoteStatus.CREATED
                "processing" -> NoteStatus.PROCESSING
                "ready" -> NoteStatus.READY
                "error" -> NoteStatus.ERROR
                else -> NoteStatus.CREATED
            },
            statusRaw = null,
            isFavorite = note.isFavorite,
            isPinned = note.isPinned,
            createdAt = note.createdAt,
            updatedAt = note.updatedAt,
        )
    }
//NoteDomain → NoteEntity + AudioEntity (Domain → Room)
    /**
     * Mapping mới: NoteDomain (Domain) -> NoteEntity + AudioEntity?
     */
    fun domainToNoteEntity(d: NoteDomain): NoteEntity = NoteEntity(
        id = d.id,
        type = when (d.type) {
            NoteType.AUDIO -> "audio"
            NoteType.TEXT -> "text"
        },
        title = d.title,
        rawText = d.rawText,
        normalizedText = d.normalizedText,
        summary = d.summary,
        keywordsJson = json.encodeToString(d.keywords),
        mindmapJson = d.mindmapJson, // here we persist serialized MindMapNode JSON
        status = when (d.status) {
            NoteStatus.CREATED -> "created"
            NoteStatus.PROCESSING -> "processing"
            NoteStatus.READY -> "ready"
            NoteStatus.ERROR -> "error"
        },
        folderId = d.folderId,
        createdAt = d.createdAt,
        updatedAt = d.updatedAt,
        isSynced = false,
        isDeleted = false,
        isFavorite = d.isFavorite,
        isPinned = d.isPinned,
    )

    fun domainToAudioEntity(d: NoteDomain): AudioEntity? {
        val audio = d.audio ?: return null
        return AudioEntity(
            noteId = d.id,
            durationSec = audio.durationSec,
            sampleRate = audio.sampleRate,
            chunksJson = json.encodeToString(audio.chunks),
            localFilePath = audio.localFilePath,
            cloudUrl = audio.cloudUrl,
            createdAt = d.createdAt,
            asrModel = null,
        )
    }
}