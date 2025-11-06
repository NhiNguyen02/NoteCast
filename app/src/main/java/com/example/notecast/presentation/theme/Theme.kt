//package com.example.notecast.presentation.theme
//
//import androidx.compose.foundation.isSystemInDarkTheme
//import androidx.compose.material3.MaterialTheme
//import androidx.compose.material3.darkColorScheme
//import androidx.compose.material3.lightColorScheme
//import androidx.compose.runtime.Composable
//import androidx.compose.ui.graphics.Color
//
//
//private val Colors = lightColorScheme(
//    primary = PrimaryColo,
//    secondary = SecondaryColor,//
//    surface = Color.White,
//    onPrimary = Color.White,
//    onSecondary = Color.Black,
//    onBackground = TextPrimary,
//    onSurface = TextPrimary
//
//)
//
//
//@Composable
//fun NoteCastTheme(
//    content: @Composable () -> Unit
//) {
//    val colors = Colors
//
//    MaterialTheme(
//        colorScheme = colors,
////            typography = Typography,
////            shapes = Shapes,
//        content = content
//    )
//}
package com.example.notecast.presentation.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColors = lightColorScheme(
    primary = PrimaryAccent,
    onPrimary = White,
    background = BgGradientStart,
    surface = White,
    onSurface = PrimaryAccentDark
)

@Composable
fun NoteCastTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LightColors,
        typography = androidx.compose.material3.Typography(),
        shapes = androidx.compose.material3.Shapes(),
        content = content
    )
}