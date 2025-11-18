package com.example.notecast.data.local.entities

import androidx.room.Embedded
import androidx.room.Relation

/**
 * Lớp POJO này giúp Room gom NoteEntity và các entity con liên quan
 * (Audio, Transcript, ProcessedText) vào một đối tượng duy nhất.
 *
 * Đây KHÔNG phải là một Entity (bảng).
 */
data class NoteWithDetails(

    // @Embedded: Báo Room lấy đối tượng NoteEntity làm "gốc"
    @Embedded
    val note: NoteEntity,

    // @Relation: Báo Room tìm trong bảng "audio"
    @Relation(
        parentColumn = "id", // Cột "id" của NoteEntity (gốc)
        entityColumn = "noteId" // Phải khớp với cột "noteId" của AudioEntity
    )
    val audio: AudioEntity?, // Nullable vì TextNote không có

    // @Relation: Báo Room tìm trong bảng "transcript"
    @Relation(
        parentColumn = "id",
        entityColumn = "noteId"
    )
    val transcript: TranscriptEntity?, // Nullable

    // @Relation: Báo Room tìm trong bảng "processed_text"
    @Relation(
        parentColumn = "id",
        entityColumn = "noteId"
    )
    val processedText: ProcessedTextEntity? // Nullable
)