package com.example.notecast.presentation.screen.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.notecast.R
import com.example.notecast.presentation.theme.backgroundPrimary
import com.example.notecast.presentation.theme.backgroundSecondary
import com.example.notecast.presentation.theme.cyan

import kotlinx.coroutines.launch

// 1. Tạo một list chứa dữ liệu cho từng trang Onboarding
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

@Composable
fun OnboardingScreen(
    onOnboardingFinished: () -> Unit // Callback khi hoàn thành Onboarding
) {
    val pagerState = rememberPagerState(pageCount = { onboardingPages.size })
    val scope = rememberCoroutineScope()
    Surface(
        modifier = Modifier
            .fillMaxSize()
            .background(brush = backgroundSecondary),
        color = Color.Transparent
    ){
        Scaffold(
            containerColor = Color.Transparent,
            modifier = Modifier.fillMaxSize(),
        ){paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .align(Alignment.Center)

                        .padding(bottom = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    HorizontalPager(
                        state = pagerState,
                        modifier = Modifier.weight(1.0f)
                    ) { page ->
                        OnboardingPage(item = onboardingPages[page])
                    }

                    Button(
                        colors = ButtonDefaults.buttonColors(
                            containerColor = cyan
                        ),
                        onClick = {
                            if (pagerState.currentPage < onboardingPages.size - 1) {
                                scope.launch {
                                    pagerState.animateScrollToPage(pagerState.currentPage + 1)
                                }
                            } else {
                                // Đã đến trang cuối, chuyển sang màn hình chính
                                onOnboardingFinished()
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp)
                            .height(56.dp)
                    ) {
                        Text(
                            text = if (pagerState.currentPage == onboardingPages.size - 1) "Bắt đầu ngay" else "Tiếp theo",

                            color = Color.White,
                            fontSize = 18.sp,

                            )
                        Icon(
                            painter = painterResource(R.drawable.outline_arrow_forward_24),
                            contentDescription = null,
                        )
                    }

                    // Text "Miễn phí + Không quảng cáo" chỉ ở trang đầu
                    if (pagerState.currentPage == 0) {
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = "Miễn phí + Không quảng cáo",
                            color = Color.LightGray
                        )
                    }else {
                        Spacer(Modifier.height(12.dp))
                    }
                }
            }
        }
    }


}

@Preview(showBackground = true)
@Composable
fun PreviewOnboardingScreen() {

    OnboardingScreen(onOnboardingFinished = {})

}