package com.example.notecast.core.audio

/**
 * Abstraction đơn giản cho buffer thread-safe dùng cho nhiều consumer.
 */
interface AudioBuffer<T> {
    /** Ghi một phần tử, có thể drop nếu đầy tuỳ implement. */
    fun write(item: T)

    /** Đọc một phần tử (blocking hoặc suspend tuỳ implement) */
    fun read(): T
    /** Xoá toàn bộ dữ liệu trong buffer để giải phóng RAM. */
    fun clear()
}
