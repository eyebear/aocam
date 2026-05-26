package com.aocam.cameranode.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val AocamLightColors: ColorScheme = lightColorScheme(
    primary = Color(0xFF155EEF),
    onPrimary = Color.White,
    secondary = Color(0xFF475467),
    background = Color(0xFFF6F7F9),
    surface = Color(0xFFFFFFFF),
    surfaceVariant = Color(0xFFE9EEF6),
    onSurface = Color(0xFF101828),
    onSurfaceVariant = Color(0xFF475467),
)

@Composable
fun AocamTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = AocamLightColors,
        content = content,
    )
}
