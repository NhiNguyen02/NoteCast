package com.example.notecast.utils

import java.util.Locale
import javax.inject.Inject

/**
 * Cấp độ 1: Heuristic Punctuator
 * Xử lý nhanh, độ trễ thấp, dùng quy tắc Regex.
 */
class HeuristicPunctuator @Inject constructor() {

    fun process(text: String): String {
        if (text.isBlank()) return ""

        var processed = text.trim()

        // 1. Viết hoa chữ cái đầu tiên
        processed = processed.replaceFirstChar {
            if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString()
        }

        // 2. Quy tắc câu hỏi (Regex cơ bản cho tiếng Việt)
        // Tìm các từ để hỏi ở cuối câu chưa có dấu
        val questionPatterns = listOf("là gì", "ở đâu", "không nhỉ", "phải không", "thế nào", "bao nhiêu")
        for (pattern in questionPatterns) {
            if (processed.endsWith(pattern, ignoreCase = true)) {
                processed += "?"
                break
            }
        }

        // 3. Nếu chưa có dấu kết thúc, mặc định thêm dấu chấm (tạm thời)
        if (!processed.endsWith(".") && !processed.endsWith("?") && !processed.endsWith("!")) {
            processed += "."
        }

        // 4. Viết hoa sau dấu chấm (cơ bản)
        val sentenceEndRegex = Regex("([.?!])\\s+([a-z])")
        processed = sentenceEndRegex.replace(processed) { matchResult ->
            val mark = matchResult.groupValues[1]
            val char = matchResult.groupValues[2]
            "$mark ${char.uppercase()}"
        }

        return processed
    }
}