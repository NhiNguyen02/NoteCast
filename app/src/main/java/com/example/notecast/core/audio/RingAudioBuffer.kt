package com.example.notecast.core.audio


/**
 * Optional interface cho các buffer hỗ trợ clear để giải phóng RAM.
 * RingAudioBuffer có thể implement interface này.
 */
interface ClearableBuffer {
    fun clear()
}
/**
 * Implementation vòng đệm (ring buffer) thread-safe cho ShortArray.
 * Dành cho VAD buffer (giữ khoảng 500ms) và recorder buffer.
 */
class RingAudioBuffer(
    capacityFrames: Int,
) : AudioBuffer<ShortArray>, ClearableBuffer {

    private val buffer: Array<ShortArray?> = arrayOfNulls(capacityFrames)
    private var head = 0
    private var tail = 0
    private var size = 0

    @Synchronized
    override fun write(item: ShortArray) {
        if (size == buffer.size) {
            // Buffer đầy: drop frame cũ nhất để ưu tiên độ trễ thấp
            tail = (tail + 1) % buffer.size
            size--
        }
        buffer[head] = item
        head = (head + 1) % buffer.size
        size++
        (this as Object).notifyAll()
    }

    @Synchronized
    override fun read(): ShortArray {
        while (size == 0) {
            try {
                (this as Object).wait()
            } catch (_: InterruptedException) {
            }
        }
        val item = buffer[tail]!!
        buffer[tail] = null
        tail = (tail + 1) % buffer.size
        size--
        return item
    }

    @Synchronized
    override fun clear() {
        for (i in buffer.indices) {
            buffer[i] = null
        }
        head = 0
        tail = 0
        size = 0
    }
}
