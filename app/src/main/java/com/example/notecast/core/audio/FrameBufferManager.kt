package com.example.notecast.core.audio
/**
 * Accumulate incoming ShortArray chunks and emit fixed-size frames to consumer callback.
 *
 * Behavior:
 *  - push(shortArray) appends samples
 *  - while buffer length >= frameSize emit frameSize samples (no overlap)
 *  - leftover samples kept for next push (no loss)
 *
 * This is simple and efficient (uses internal ShortArray and head index).
 */
class FrameBufferManager(initialCapacity: Int = 16000) {
    private var buffer = ShortArray(initialCapacity)
    private var writePos = 0
    private var readPos = 0

    private fun ensureCapacity(additional: Int) {
        val free = buffer.size - writePos
        if (free >= additional) return
        val used = writePos - readPos
        val newSize = (buffer.size + additional).coerceAtLeast(buffer.size * 2)
        val newBuf = ShortArray(newSize)
        if (used > 0) {
            System.arraycopy(buffer, readPos, newBuf, 0, used)
        }
        buffer = newBuf
        writePos = used
        readPos = 0
    }

    /**
     * Append incoming chunk (copy).
     */
    fun push(chunk: ShortArray) {
        ensureCapacity(chunk.size)
        System.arraycopy(chunk, 0, buffer, writePos, chunk.size)
        writePos += chunk.size
    }

    /**
     * If there is a full frame available, pop it (consume) and return it.
     * Returns null if not enough samples.
     */
    fun popFrame(frameSize: Int): ShortArray? {
        val available = writePos - readPos
        if (available < frameSize) return null
        val out = ShortArray(frameSize)
        System.arraycopy(buffer, readPos, out, 0, frameSize)
        readPos += frameSize
        // If buffer consumed fully, reset pointers to avoid unbounded growth
        if (readPos == writePos) {
            readPos = 0
            writePos = 0
        } else if (readPos > buffer.size / 2) {
            // compact buffer periodically
            val remaining = writePos - readPos
            System.arraycopy(buffer, readPos, buffer, 0, remaining)
            readPos = 0
            writePos = remaining
        }
        return out
    }

    /**
     * Convenience: flush all remaining samples into single ShortArray and clear buffer.
     */
    fun flushAll(): ShortArray {
        val available = writePos - readPos
        val out = ShortArray(available)
        if (available > 0) {
            System.arraycopy(buffer, readPos, out, 0, available)
        }
        readPos = 0
        writePos = 0
        return out
    }

    fun availableSamples(): Int = writePos - readPos
}
