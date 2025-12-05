package com.example.notecast.domain.vad

import com.example.notecast.domain.model.AsrChunk

/**
 * Các event segment audio sau khi VAD + Segmenter xử lý.
 * Dùng làm cầu nối sang pipeline ASR.
 */
sealed class SegmentEvent {
    /** Không có thay đổi segment đáng chú ý cho frame hiện tại. */
    object None : SegmentEvent()

    /** Bắt đầu một đoạn speech mới. */
    object Start : SegmentEvent()

    /** Đang tiếp tục nằm trong một đoạn speech. */
    object Continue : SegmentEvent()

    /** Kết thúc một đoạn speech, kèm theo toàn bộ chunk PCM (float 16kHz) của đoạn đó + metadata thời gian. */
    data class End(val chunk: AsrChunk) : SegmentEvent()
}