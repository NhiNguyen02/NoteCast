package com.example.notecast.domain.usecase.postprocess

import com.example.notecast.data.repository.AiRepository
import com.example.notecast.domain.model.ProcessedTextData
import com.example.notecast.utils.HeuristicPunctuator
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class NormalizeNoteUseCase @Inject constructor(
    private val aiRepository: AiRepository,
    private val heuristicPunctuator: HeuristicPunctuator
) {
    operator fun invoke(content: String): Flow<NormalizationResult> = flow {
        // 1. Cấp độ 1: Heuristic (Nhanh)
        val fastResult = heuristicPunctuator.process(content)
        emit(NormalizationResult.Preview(fastResult))

        // 2. Cấp độ 2: Neural AI (Chậm, Chính xác)
        try {
            val data = aiRepository.processNlpPostProcessing(content)

            // Trả về kết quả thành công
            emit(NormalizationResult.Success(data))
            // -------------------
        } catch (e: Exception) {
            // Nếu lỗi, dùng tạm kết quả Heuristic
            emit(NormalizationResult.Error(fastResult))
        }
    }
}

// Sealed class để ViewModel nhận biết
sealed class NormalizationResult {
    data class Preview(val text: String) : NormalizationResult()
    data class Success(val data: ProcessedTextData) : NormalizationResult() // Chứa cả text và keywords
    data class Error(val text: String) : NormalizationResult()
}