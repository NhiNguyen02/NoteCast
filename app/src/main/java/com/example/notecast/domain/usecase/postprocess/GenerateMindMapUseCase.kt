package com.example.notecast.domain.usecase.postprocess

import com.example.notecast.data.repository.AiRepository
import com.example.notecast.domain.model.MindMapNode
import com.example.notecast.domain.model.Note
import javax.inject.Inject

class GenerateMindMapUseCase @Inject constructor(
    private val aiRepository: AiRepository
) {
    suspend operator fun invoke(note: Note): MindMapNode {
        val textToProcess = note.content?.takeIf { it.isNotBlank() }
            ?: note.rawText?.takeIf { it.isNotBlank() }
            ?: note.title

        if (textToProcess.isBlank()) return MindMapNode(label = "Trống", colorHex = "#808080")

        return aiRepository.generateMindMap(textToProcess)
    }
}