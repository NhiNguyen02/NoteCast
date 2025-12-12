package com.example.notecast.domain.usecase.postprocess

import com.example.notecast.data.repository.SummaryRepository
import javax.inject.Inject

class SummarizeNoteUseCase @Inject constructor(
    private val repository: SummaryRepository
) {

    /**
     * Thực hiện tóm tắt ghi chú bằng LLM.
     * @param noteContent nội dung ghi chú gốc.
     * @return string đã được tóm tắt.
     */
    suspend operator fun invoke(noteContent: String): String {
        return repository.summarize(noteContent)
    }
}