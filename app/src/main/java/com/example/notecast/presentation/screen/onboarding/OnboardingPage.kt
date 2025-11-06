package com.example.notecast.presentation.screen.onboarding

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.notecast.R // Import R của project

// Data class để định nghĩa nội dung cho mỗi trang Onboarding
data class OnboardingItem(
    val imageResId: Int, // ID của icon chính (micro, sóng âm, não AI)
    val title: String,
    val text: String,
    val features: List<Pair<Int, String>> = emptyList() // List các icon + text nhỏ dưới
)

@Composable
fun OnboardingPage(item: OnboardingItem, modifier: Modifier = Modifier) {
    Column(
//        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween, // Căn giữa nội dung

    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center // Căn giữa nội dung
        ) {
            // Icon chính ở trên
            Image(
                painter = painterResource(id = item.imageResId),
                contentDescription = null,
                modifier = Modifier.size(320.dp) // Kích thước icon lớn
            )
            Spacer(Modifier.height(20.dp))

            // Tiêu đề
            Text(
                text = item.title,
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 24.dp)
            )
            Spacer(Modifier.height(20.dp))

            // Mô tả
            Text(
                text = item.text,
                style = MaterialTheme.typography.bodyLarge.copy(
                    color = Color.White
                ),
                fontSize = 15.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 24.dp)
            )
            Spacer(Modifier.height(40.dp))
        }

        // Các icon + text nhỏ
        if (item.features.isNotEmpty()) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,

            ) {
                item.features.forEach { (iconResId, featureText) ->
                    Spacer(Modifier.width(15.dp))
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,

                    ) {
                        Image(
                            painter = painterResource(id = iconResId),
                            contentDescription = null,
                            modifier = Modifier.size(44.dp)
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = featureText,
                            color = Color.White,
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
        // Spacer cho phần dưới để nút bấm không bị dính vào text
        Spacer(Modifier.height(100.dp))
    }
}
