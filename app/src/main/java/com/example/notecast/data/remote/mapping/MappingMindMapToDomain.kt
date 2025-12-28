package com.example.notecast.data.remote.mapping

import com.example.notecast.data.remote.dto.MindmapDto
import com.example.notecast.domain.model.MindMapNode
import com.example.notecast.domain.model.NoteDomain

/**
 * Sanitize MindmapDto từ backend sang cây MindMapNode domain an toàn.
 * - Trả về null nếu thiếu root hoặc label rỗng.
 * - Đảm bảo children != null.
 * - Giữ nguyên colorHex nếu có; nếu null thì tự fill màu mặc định.
 */
fun MindmapDto?.toSanitizedRootOrNull(): MindMapNode? {
    val rawRoot = this?.root ?: return null

    // sanitize đệ quy
    fun sanitize(node: MindMapNode): MindMapNode? {
        val label = node.label.trim()
        if (label.isEmpty()) return null

        val safeChildren = node.children.orEmpty()
            .mapNotNull { child -> sanitize(child) }

        val safeColor = node.colorHex ?: "#6200EE"

        return MindMapNode(
            // id: nếu node.id null hoặc rỗng, để default ctor sinh UUID mới
            label = label,
            children = safeChildren,
            colorHex = safeColor,
        )
    }

    return sanitize(rawRoot)
}
