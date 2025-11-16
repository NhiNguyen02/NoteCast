package com.example.notecast.data.local.mapper

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.core.graphics.toColorInt
import com.example.notecast.data.local.entities.*
import com.example.notecast.domain.model.* // Đảm bảo import đúng 5 model
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * Mapper giữa Room entities và domain models sử dụng kotlinx.serialization.
 */
object EntityMapper {
    private val json = Json {
        encodeDefaults = true
        ignoreUnknownKeys = true
        prettyPrint = false
    }

    // --- Audio ---
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

    // --- Note ---
    fun noteEntityToDomain(e: NoteEntity) = Note(
        id = e.id,
        title = e.title,
        content = e.content ?: "", // SỬA: Thêm content (từ NoteEntity)
        processedTextId = e.processedTextId,
        // (processedText sẽ được điền bởi logic Repository/UseCase)
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
        content = d.content, // SỬA: Thêm content (từ Note Model)
        processedTextId = d.processedTextId,
        tags = json.encodeToString(d.tags),
        isFavorite = d.isFavorite,
        folderId = d.folderId,
        colorHex = d.colorHex,
        updatedAt = d.updatedAt,
        isSynced = d.isSynced,
        isDeleted = d.isDeleted
    )

    // --- Transcript (SỬA: HOÀN THIỆN TODO) ---
    fun transcriptEntityToDomain(e: TranscriptEntity) = Transcript(
        id = e.id,
        audioId = e.audioId,
        rawText = e.rawText,
        language = e.language,
        confidence = e.confidence,
        createdAt = e.createdAt,
        isSynced = e.isSynced,
        isDeleted = e.isDeleted
    )

    fun domainToTranscriptEntity(d: Transcript) = TranscriptEntity(
        id = d.id,
        audioId = d.audioId,
        rawText = d.rawText,
        language = d.language,
        confidence = d.confidence,
        createdAt = d.createdAt,
        isSynced = d.isSynced,
        isDeleted = d.isDeleted
    )

    // --- ProcessedText (SỬA: HOÀN THIỆN TODO) ---
    fun processedTextEntityToDomain(e: ProcessedTextEntity) = ProcessedText(
        id = e.id,
        transcriptId = e.transcriptId,
        punctuatedText = e.punctuatedText,
        summary = e.summary,
        sentiment = e.sentiment,
        createdAt = e.createdAt,
        isSynced = e.isSynced,
        isDeleted = e.isDeleted
    )

    fun domainToProcessedTextEntity(d: ProcessedText) = ProcessedTextEntity(
        id = d.id,
        transcriptId = d.transcriptId,
        punctuatedText = d.punctuatedText,
        summary = d.summary,
        sentiment = d.sentiment,
        createdAt = d.createdAt,
        isSynced = d.isSynced,
        isDeleted = d.isDeleted
    )

    // --- Folder (SỬA: HOÀN THIỆN TODO) ---
    fun folderEntityToDomain(e: FolderEntity) = Folder(
        id = e.id,
        name = e.name,
        colorHex = e.colorHex,
        createdAt = e.createdAt,
        updatedAt = e.updatedAt,
        isSynced = e.isSynced,
        isDeleted = e.isDeleted
    )

    fun domainToFolderEntity(d: Folder): FolderEntity {
        return FolderEntity(
            id = d.id,
            name = d.name,
            colorHex = d.colorHex,
            createdAt = d.createdAt,
            updatedAt = d.updatedAt,
            isSynced = d.isSynced,
            isDeleted = d.isDeleted
        )
    }
}