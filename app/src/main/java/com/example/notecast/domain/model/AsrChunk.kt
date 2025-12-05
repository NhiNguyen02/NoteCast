package com.example.notecast.domain.model

// MergeChunksUseCase (dựa trên startSec/endSec).
//Mapping transcript → waveform/timeline UI trong tương lai.
data class AsrChunk(
    val startSec: Double,
    val endSec: Double,
//    val startSample: Long,
//    val endSample: Long,
    val samples: FloatArray
)