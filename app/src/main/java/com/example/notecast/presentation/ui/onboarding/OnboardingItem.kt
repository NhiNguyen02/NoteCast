package com.example.notecast.presentation.ui.onboarding

import com.example.notecast.R

data class OnboardingItem(
    val imageResId: Int, // ID của icon chính (micro, sóng âm, não AI)
    val title: String,
    val text: String,
    val features: List<Pair<Int, String>> = emptyList() // List các icon + text nhỏ dưới
)

val onboardingPages = listOf(
    OnboardingItem(
        imageResId = R.drawable.onboarding_welcome,
        title = "Khám phá Sức mạnh ghi chú giọng nói",
        text = "Chuyển đổi giọng nói thành văn bản thông minh với công nghệ AI tiên tiến",
        features = listOf(
            R.drawable.zap_icon to "Tức thì", // Thay bằng icon của bạn
            R.drawable.shield_check_icon to "Bảo mật",
            R.drawable.brain_circuit_icon to "Thông minh"
        )
    ),
    OnboardingItem(
        imageResId = R.drawable.onboarding_transcription, // Thay bằng icon sóng âm của bạn
        title = "Chép lời Thông minh",
        text = "Chuyển đổi giọng nói thành văn bản với độ chính xác cao và hỗ trợ nhiều ngôn ngữ",
        features = listOf(
            R.drawable.zap_icon to "Tức thì",
            R.drawable.globe_icon to "Đa ngôn ngữ",
            R.drawable.wifi_off_icon to "Offline"
        )
    ),
    OnboardingItem(
        imageResId = R.drawable.onboarding_ai, // Thay bằng icon não AI của bạn
        title = "Xử lý & Phân tích",
        text = "Các công cụ AI tiên tiến giúp xử lý và phân tích nội dung một cách thông minh và hiệu quả",
        features = listOf(
            R.drawable.zap_icon to "Nhanh chóng",
            R.drawable.target_icon to "Chính xác",
            R.drawable.cpu_icon to "Tự động"
        )
    )
)