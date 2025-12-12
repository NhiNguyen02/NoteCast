package com.example.notecast.domain.usecase

import kotlinx.coroutines.delay
import java.io.File
import javax.inject.Inject

class TranscribeAudioUseCase @Inject constructor() {
    /**
     * Hàm này nhận vào file audio và trả về văn bản.
     * Hiện tại đang GIẢ LẬP (Simulate) quá trình xử lý mất 3 giây.
     */
    suspend operator fun invoke(audioFile: File): String {
        // Giai đoạn 2 chúng ta sẽ viết code chạy ONNX ở đây.

        // Giả lập thời gian xử lý (3 giây)
        delay(3000)

        // Trả về văn bản giả lập
        return "Đây là văn bản mẫu được chuyển đổi từ giọng nói. Sau khi bạn tích hợp API thật, nội dung thực tế của file ghi âm sẽ xuất hiện ở đây. Hệ thống đã xử lý thành công!"
    }
}