package com.example.notecast.domain.usecase.audio

import com.example.notecast.domain.repository.AudioRepository
import com.example.notecast.domain.vad.SegmentEvent
import com.example.notecast.domain.vad.VADDetector
import com.example.notecast.core.vad.Segmenter
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import javax.inject.Inject

/**
 * VadSegmenterUseCase
 * - Lấy PCM16 16kHz từ AudioRepository.asrPcmFrames()
 * - Chạy VADDetector trên từng frame để có isSpeech
 * - Đưa frame + isSpeech vào Segmenter
 * - Emit SegmentEvent ra Flow<SegmentEvent> cho ViewModel/ASR.
 */
class VadSegmenterUseCase @Inject constructor(
    private val audioRepository: AudioRepository,
    private val vadDetector: VADDetector,
    private val segmenter: Segmenter,
) {
    operator fun invoke(): Flow<SegmentEvent> = channelFlow {
        audioRepository.asrPcmFrames().collect { frame ->
            // đảm bảo frame không rỗng
            if (frame.isEmpty()) return@collect

            val isSpeech = vadDetector.isSpeech(frame)
            val event = segmenter.process(frame, isSpeech)

            if (event !is SegmentEvent.None) {
                trySend(event)
            }
        }
    }
}

