package com.example.notecast.presentation.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = Purple80,
    secondary = PurpleGrey80,
    tertiary = Pink80
)

private val LightColorScheme = lightColorScheme(
    // Màu Tím/Xanh tím là màu chủ đạo (Primary)
    primary = Purple,
    // Màu nền cho các thành phần UI nổi (Secondary)
    secondary = Blue,

    // Màu khi có lỗi
    error = Red,

    // Nền tổng thể: White (vì noteItemBackground là White với alpha)
    background = Color.White,
    // Nền của các "Surface" (Card, Sheets, Dialogs)
    surface = NoteItemBackground,

    // Màu chữ trên các nền tương ứng
    onPrimary = Color.White, // Giả định chữ trên Primary (Purple) là màu Trắng
    onSecondary = Color.White, // Giả định chữ trên Secondary (Blue) là màu Trắng
    onBackground = TitleNoteColor, // Màu chữ chính trên nền Background
    onSurface = TextNoteColor,     // Màu chữ phụ trên nền Surface

    // Màu cho các Icon/Outline
    outline = OutlineColor,
    // Màu nền cho các thành phần được nhấn mạnh
    surfaceVariant = LabelColor
)

@Composable
fun NoteCastTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}