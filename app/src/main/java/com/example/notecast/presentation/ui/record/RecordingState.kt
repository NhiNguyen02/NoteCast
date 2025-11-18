package com.example.notecast.presentation.ui.record

enum class RecordingState {
    Idle,           // Chưa ghi / đã kết thúc
    Recording,      // Đang ghi
    Paused,         // Đã tạm dừng nhưng chưa stop
    Stopping        // Đang kết thúc (flush buffer, finalize)
}