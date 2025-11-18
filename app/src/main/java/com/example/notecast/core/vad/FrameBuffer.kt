package com.example.notecast.core.vad

/**
 * Buffer gom samples → frame đúng size, không bỏ mẫu
 */
class FrameBuffer {
    private var expected = 512
    private val buffer = kotlin.collections.ArrayList<Short>()

    fun configure(expectedSize: Int) {
        expected = expectedSize
        buffer.clear()
    }

    fun push(frame: ShortArray, onFullFrame: (ShortArray) -> Unit) {
        buffer.addAll(frame.toList())
        while (buffer.size >= expected) {
            val full = buffer.take(expected).toShortArray()
            onFullFrame(full)
            repeat(expected) { buffer.removeAt(0) }
        }
    }
}