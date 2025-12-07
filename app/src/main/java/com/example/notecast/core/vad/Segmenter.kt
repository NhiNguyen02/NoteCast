package com.example.notecast.core.vad

import com.example.notecast.domain.model.AsrChunk
import com.example.notecast.domain.vad.SegmentEvent

/**
 * Segmenter đơn giản dựa trên cờ speech/no-speech từ VAD.
 *- Giữ preroll history (vài frame trước khi bắt đầu nói).
 *- Dùng hangover để tránh cắt câu quá sớm.
 */
class Segmenter(
    private val sampleRate: Int = 16_000,
    private val frameSize: Int,
    hangoverMs: Int = 300,
    prerollMs: Int = 150,
) {
    private val prerollFramesCount: Int = ((prerollMs / 1000.0) * sampleRate / frameSize).toInt().coerceAtLeast(1)
    private val hangoverFramesCount: Int = ((hangoverMs / 1000.0) * sampleRate / frameSize).toInt().coerceAtLeast(1)

    // Vùng đệm giữ các frame gần nhất để preroll
    private val prerollBuffer: ArrayDeque<ShortArray> = ArrayDeque()

    // Các frame hiện tại đang thuộc một segment speech
    private val currentSpeechFrames: MutableList<ShortArray> = mutableListOf()

    private var inSpeech: Boolean = false
    private var silenceFramesWhileInSpeech: Int = 0
    private var totalFramesProcessed: Long = 0L

    /**
     * Xử lý một frame PCM + cờ isSpeech.
     * Trả về SegmentEvent nếu có sự kiện START/CONTINUE/END, ngược lại trả None.
     */
    fun process(frame: ShortArray, isSpeech: Boolean): SegmentEvent {
        // Mỗi lần vào process nghĩa là ta đã thấy thêm 1 frame mới
        // nên tăng totalFramesProcessed ở đầu để dùng làm mốc thời gian chính xác.
        totalFramesProcessed += 1

        // Cập nhật preroll buffer (luôn giữ history gần nhất)
        if (prerollBuffer.size >= prerollFramesCount) {
            prerollBuffer.removeFirst()
        }
        prerollBuffer.addLast(frame)

        if (!inSpeech) {
            if (isSpeech) {
                // Bắt đầu đoạn speech mới: lôi tất cả preroll vào đoạn hiện tại
                inSpeech = true
                silenceFramesWhileInSpeech = 0
                currentSpeechFrames.clear()
                currentSpeechFrames.addAll(prerollBuffer)
                return SegmentEvent.Start
            }
            return SegmentEvent.None
        }

        // Đang trong speech
        return if (isSpeech) {
            silenceFramesWhileInSpeech = 0
            currentSpeechFrames.add(frame)
            SegmentEvent.Continue
        } else {
            silenceFramesWhileInSpeech += 1
            // Nếu chưa đủ hangover → vẫn coi là đang nói
            if (silenceFramesWhileInSpeech < hangoverFramesCount) {
                currentSpeechFrames.add(frame)
                SegmentEvent.Continue
            } else {
                // Kết thúc segment
                inSpeech = false
                silenceFramesWhileInSpeech = 0

                // Gộp toàn bộ các frame trong currentSpeechFrames thành một FloatArray
                val totalSamples = currentSpeechFrames.sumOf { it.size }
                val out = FloatArray(totalSamples)
                var pos = 0
                for (f in currentSpeechFrames) {
                    for (s in f) {
                        out[pos++] = s.toFloat() / Short.MAX_VALUE.toFloat()
                    }
                }
                currentSpeechFrames.clear()

                // Tính toán thời gian chính xác hơn, có xét đến preroll + hangover:
                val segmentSamples = out.size
                val endSampleExclusive = totalFramesProcessed * frameSize
                val startSampleInclusive = (endSampleExclusive - segmentSamples).coerceAtLeast(0)
                val startSec = startSampleInclusive.toDouble() / sampleRate
                val endSec = endSampleExclusive.toDouble() / sampleRate

                val asrChunk = AsrChunk(
                    startSec = startSec,
                    endSec = endSec,
                    startSample = startSampleInclusive,
                    endSample = endSampleExclusive,
                    samples = out,
                )
                return SegmentEvent.End(asrChunk)
            }
        }
    }
}
