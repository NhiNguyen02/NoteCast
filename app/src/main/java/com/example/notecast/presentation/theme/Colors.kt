package com.example.notecast.presentation.theme


import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.LinearGradient

val backgroundPrimary = Brush.verticalGradient(colors = listOf(
    Color(0xFFE6CEFF), // Màu Tím (trên)
    Color(0xFF9BC6FB)  // Màu Xanh (dưới)
))
val backgroundSecondary = Brush.verticalGradient(

0.0f to Color(0xFFCCA8FF),    // 0%
    0.42f to Color(0xFFA363FF),   // 42%
    0.62f to Color(0xFF8F40FF),  // 62%
    0.83f to Color(0xFF852EFF),   // 83%
    1.0f to  Color(0xFF7B1DFF),
)
val textGradient = Brush.horizontalGradient(
    0.0f to Color(0xFF6E3FCD),
    0.41f to Color(0xFF8A4AE1),
    0.73f to Color(0xFF308BFD),
    1.0f to Color(0xFF00D2FF),
)

val cyan = Color(0xff4DD0E1)
val lightPurple = Color(0xFF6200AE)
val backgroundTertiary = Brush.verticalGradient(
    0.0f to Color(0xFF00D2FF),
    0.37f to Color(0xFF307FE3),
    0.71f to Color(0xFF7532FB),
    0.95f to Color(0xFF8A4AE1),
)
// Colors tuned to match the Figma screenshot
val BgGradientStart = Color(0xFFF5E9FF) // very light purple top
val BgGradientEnd = Color(0xFFDFF6FF)   // light blue-ish bottom

val PrimaryAccent = Color(0xFF7B4BFF)      // purple used for selected chips and apply button
val PrimaryAccentDark = Color(0xFF5B2FE0)  // darker purple for title text / accents
val White = Color(0xFFFFFFFF)
val SurfaceGray = Color(0xFFF6F6FB)
val TextPrimary = Color(0xFF222222)
val TextSecondary = Color(0xFF6B6B6B)
val BadgePurple = Color(0xFF6E47F8)

val SuccessGreen = Color(0xFF2FD08B)
val DangerRed = Color(0xFFFF4D6D)