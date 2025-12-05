package com.example.notecast.domain.usecase

import com.example.notecast.domain.model.ChunkResult
import javax.inject.Inject

/**
 * MergeChunksUseCase (skeleton)
 *
 * Mục tiêu: Ghép text từ nhiều chunk ASR có overlap thời gian (ví dụ chunk 30s, stride 5s).
 *
 * Thiết kế sau này có thể dựa trên:
 * - Token-level timestamps (nếu decoder trả về).
 * - Hoặc heuristic longest-overlap trên chuỗi text + confidence.
 *
 * Hiện tại chỉ giữ skeleton để sau này thay thế việc joinToString(" ") trong ProcessAudioUseCase.
 */
class MergeChunksUseCase @Inject constructor() {

    // Heuristic đơn giản:
    // - Sắp xếp theo startSec.
    // - Nếu hai đoạn overlap > 1.0s, cố gắng tránh lặp từ bằng cách không thêm nguyên text của đoạn sau
    //   mà chỉ append phần "đuôi" chưa xuất hiện (simple suffix overlap).
    // - Nếu không overlap hoặc chồng lấn ít, nối text thô bằng khoảng trắng.
    operator fun invoke(chunks: List<ChunkResult>): String {
        if (chunks.isEmpty()) return ""

        val sorted = chunks.sortedBy { it.startSec }
        val sb = StringBuilder()

        fun appendWithSpace(s: String) {
            if (sb.isNotEmpty() && s.isNotBlank()) {
                sb.append(' ')
            }
            sb.append(s)
        }

        appendWithSpace(sorted.first().text.trim())
        var lastEnd = sorted.first().endSec

        for (i in 1 until sorted.size) {
            val current = sorted[i]
            val overlap = (lastEnd - current.startSec).coerceAtMost(current.endSec - current.startSec)
            val text = current.text.trim()
            if (text.isEmpty()) continue

            if (overlap > 1.0) {
                // Simple suffix overlap heuristic: nếu sb kết thúc bằng prefix của text,
                // chỉ thêm phần còn lại để tránh lặp.
                val existing = sb.toString()
                val maxCheck = minOf(existing.length, text.length)
                var bestSuffixIdx = 0
                for (k in maxCheck downTo 1) {
                    val suffix = existing.takeLast(k)
                    if (text.startsWith(suffix)) {
                        bestSuffixIdx = k
                        break
                    }
                }
                val tail = text.drop(bestSuffixIdx)
                appendWithSpace(tail)
            } else {
                appendWithSpace(text)
            }
            lastEnd = maxOf(lastEnd, current.endSec)
        }

        return sb.toString().trim()
    }
}
