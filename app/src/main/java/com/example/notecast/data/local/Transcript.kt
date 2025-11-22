package com.example.notecast.data.local

data class Transcript(
    val text: String,
    val timestamp: Long = System.currentTimeMillis()
)