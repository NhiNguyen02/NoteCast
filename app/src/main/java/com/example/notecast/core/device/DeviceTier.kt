package com.example.notecast.core.device

enum class DeviceTier {
    LOW_END,      // chọn WebRTC/RMS
    MID_HIGH_END  // chọn Silero (ONNX) nếu có
}