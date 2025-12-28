package com.example.notecast.presentation.ui.dialog

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.notecast.presentation.theme.PopUpBackgroundBrush

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProcessingDialog(
    title: String,
    onDismissRequest: () -> Unit,
) {
    val dialogWidth = 320.dp
    val dialogCorner = 20.dp
    val innerPadding = 18.dp

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.36f)),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            modifier = Modifier
                .width(dialogWidth)
                .wrapContentHeight(),
            shape = RoundedCornerShape(dialogCorner),
            color = Color.Transparent,
            tonalElevation = 12.dp,
            onClick = { /* block outside taps */ }
        ) {
            Box(
                modifier = Modifier
                    .background(brush = PopUpBackgroundBrush, shape = RoundedCornerShape(dialogCorner))
                    .padding(innerPadding)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = title,
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 15.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    CircularProgressIndicator(
                        color = Color.White,
                        strokeWidth = 3.dp,
                        modifier = Modifier.size(40.dp)
                    )
                }
            }
        }
    }
}