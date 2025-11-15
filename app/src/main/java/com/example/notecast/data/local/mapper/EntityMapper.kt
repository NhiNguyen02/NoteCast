package com.example.notecast.data.local.mapper

import com.example.notecast.data.local.entities.*
import com.example.notecast.domain.model.* // assumes domain models exist
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * Mapper between Room entities and domain models using kotlinx.serialization for JSON.
 * Keeps no reflection-heavy libs and aligns with Compose-friendly kotlin serialization setup.
 */
object EntityMapper {
    private val json = Json {
        encodeDefaults = true
        ignoreUnknownKeys = true
        prettyPrint = false
    }

    fun audioEntityToDomain(e: AudioEntity) = Audio(
        id = e.id,
        filePath = e.filePath,
        cloudUrl = e.cloudUrl,
        durationMs = e.durationMs,
        sampleRate = e.sampleRate,
        channels = e.channels,
        createdAt = e.createdAt,
        isSynced = e.isSynced,
        isDeleted = e.isDeleted
    )

    fun domainToAudioEntity(d: Audio) = AudioEntity(
        id = d.id,
        filePath = d.filePath,
        cloudUrl = d.cloudUrl,
        durationMs = d.durationMs,
        sampleRate = d.sampleRate,
        channels = d.channels,
        createdAt = d.createdAt,
        isSynced = d.isSynced,
        isDeleted = d.isDeleted
    )

    fun noteEntityToDomain(e: NoteEntity) = Note(
        id = e.id,
        title = e.title,
        processedTextId = e.processedTextId,
        tags = if (e.tags.isBlank()) emptyList() else json.decodeFromString(e.tags),
        isFavorite = e.isFavorite,
        folderId = e.folderId,
        colorHex = e.colorHex,
        updatedAt = e.updatedAt,
        isSynced = e.isSynced,
        isDeleted = e.isDeleted
    )

    fun domainToNoteEntity(d: Note) = NoteEntity(
        id = d.id,
        title = d.title,
        processedTextId = d.processedTextId,
        tags = json.encodeToString(d.tags),
        isFavorite = d.isFavorite,
        folderId = d.folderId,
        colorHex = d.colorHex,
        updatedAt = d.updatedAt,
        isSynced = d.isSynced,
        isDeleted = d.isDeleted
    )

    // TODO: add mappings for TranscriptEntity <-> Transcript and ProcessedTextEntity <-> ProcessedText
}