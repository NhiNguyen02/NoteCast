package com.example.notecast.data.local.entities

import androidx.room.Embedded
import androidx.room.Relation


/**
 * Projection đơn giản cho NoteWithAudio sử dụng NoteEntity và AudioEntity mới,
 * để thay thế cho việc kết hợp NoteWithDetails/Transcript/ProcessedText trong tương lai.
 */
data class NoteWithAudio(
    @Embedded val note: NoteEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "noteId",
    )
    val audio: AudioEntity?,
)
