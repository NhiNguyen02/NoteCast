package com.example.notecast.domain.usecase

import java.io.File

data class ProcessResult(
    val file: File?,
    val recordedMs: Long,
    val message: String? = null
)