package com.example.notecast.presentation.ui.onboarding

import androidx.compose.foundation.background
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
import com.example.notecast.presentation.theme.Cyan
import com.example.notecast.R
import com.example.notecast.presentation.theme.OnboardingBackgroundBrush

import kotlinx.coroutines.launch

@Composable
fun OnboardingScreen(
    onOnboardingFinished: () -> Unit // Callback khi hoàn thành Onboarding
) {
    val pagerState = rememberPagerState(pageCount = { onboardingPages.size })
    val scope = rememberCoroutineScope()
    Surface(
        modifier = Modifier
            .fillMaxSize()
            .background(brush = OnboardingBackgroundBrush),
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
                            containerColor = Cyan
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
                            painter = painterResource(R.drawable.arrow_forward_long_svgrepo_com),
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