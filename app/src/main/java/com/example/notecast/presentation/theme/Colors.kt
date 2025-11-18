package com.example.notecast.presentation.theme

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

val Purple80 = Color(0xFFD0BCFF)
val PurpleGrey80 = Color(0xFFCCC2DC)
val Pink80 = Color(0xFFEFB8C8)

val Purple40 = Color(0xFF6650a4)
val PurpleGrey40 = Color(0xFF625b71)
val Pink40 = Color(0xFF7D5260)

// Màu đơn (Single Colors)
val BluePurple = Color(0xFF6E3FCD)
val Purple = Color(0xFF8A4AE1)
val Blue = Color(0xFF308BFD)
val Cyan = Color(0xFF00D2FF)
val Red = Color(0xFFEF4444)
val LightGreen = Color(0xFF5AE0B4)

// Màu chức năng (Functional Colors)
val SubTitleColor = Color(0xFF5600CE)
val IconColor = Color(0xFF8630FF)
val IconOnPopUpColor = Color(0xFF6200AE)
val UnableIconColor = Color(0xFFD1D5DB)
val LabelColor = Color(0xFFBDCAE4)
val OutlineColor = Color(0xFF8B5CF6)
val TitleNoteColor = Color(0xFF000000)
val TextNoteColor = Color(0xFF757575)
val PopUpContentBackgroundColor = Color(0xFF5600CE).copy(alpha = 0.25f)
val NoteItemBackground = Color(0xFFFFFFFF).copy(alpha = 0.7f)
val PrimaryAccent = Color(0xFF7B4BFF)
val PrimaryAccentDark = Color(0xFF5B2FE0)  // darker purple for title text / accents

// Gradients (Brushes) - Khai báo cho việc sử dụng trực tiếp trong UI
// Linear Gradient Brush từ trái sang phải (ngang)
val LogoBrush = Brush.linearGradient(
    colors = listOf(BluePurple, Purple, Blue, Cyan)
)

// Linear Gradient Brush từ trên xuống dưới (dọc)
val Background = Brush.linearGradient(
    colors = listOf(Color(0xFFE6CEFF), Color(0xFF9BC6FB)),
    start = Offset.Zero,
    end = Offset(0f, 1000f)
)

val OnboardingBackgroundBrush = Brush.linearGradient(
    colors = listOf(Color(0xFFCCA8FF), Color(0xFFA363FF), Color(0xFF8F40FF), Color(0xFF852EFF), Color(0xFF7B1DFF)),
    start = Offset.Zero,         // Điểm bắt đầu (Trên, X=0, Y=0)
    end = Offset(0f, 1000f)      // Điểm kết thúc (Dưới, giữ X=0f để nằm dọc)
)

val MainBackgroundBrush = Brush.linearGradient(
    colors = listOf(Color(0xFFE6CEFF), Color(0xFF9BC6FB)),
    start = Offset.Zero,
    end = Offset(0f, 1000f)
)

val PopUpBackgroundBrush = Brush.linearGradient(
    colors = listOf(Color(0xFFCCA8FF), Color(0xFFA363FF), Color(0xFF8F40FF), Color(0xFF852EFF), Color(0xFF7B1DFF)),
    start = Offset.Zero,
    end = Offset(0f, 1000f)
)

val TitleBrush = Brush.verticalGradient(
    colors = listOf(
        Color(0xFF00D2FF),
        Color(0xFF307FE3),
        Color(0xFF7532FB),
        Color(0xFF8A4AE1)
    )
)

val MainButtonBrush = Brush.verticalGradient(
    colors = listOf(
        Color(0xFF00D2FF),
        Color(0xFF307FE3),
        Color(0xFF7532FB),
        Color(0xFF8A4AE1)
    )
)

val MenuBackgroundBrush = Brush.linearGradient(
    colors = listOf(Color(0xFF00D2FF), Color(0xFF307FE3), Color(0xFF7532FB), Color(0xFF8A4AE1)),
    start = Offset.Zero,
    end = Offset(0f, 1000f)
)

val TabButton1Brush = Brush.verticalGradient(
    colors = listOf(Color(0xFFC2D1EC), Color(0xFF6A92C8))
)

val TabButton2Brush = Brush.verticalGradient(
    colors = listOf(Color(0xFF4AC5EE), Color(0xFF5C6FD5))
)

val TabButton3Brush = Brush.verticalGradient(
    colors = listOf(Color(0xFFFFF1F0), Color(0xFFAFE1F8))
)

val TabButton4Brush = Brush.verticalGradient(
    colors = listOf(
        Color(0xFFCCA8FF),
        Color(0xFFA363FF),
        Color(0xFF8F40FF),
        Color(0xFF852EFF),
        Color(0xFF7B1DFF)
    )
)

val FooterGradientBrush = Brush.verticalGradient(
    listOf(
        Color(0xFFB96CFF),
        Color(0xFF8A4BFF),
        Color(0xFF6A2CFF)
    )
)
