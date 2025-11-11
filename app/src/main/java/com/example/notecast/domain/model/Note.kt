package com.example.notecast.domain.model

import java.util.Date

data class Note(
    val id: Int = 0,
    val title: String = "",
    val content: String = "",
    val folder: Folder? = null,
    val isPinned: Boolean = false,
    val isFavorite: Boolean = false,
    val lastEdited: Date = Date()
)