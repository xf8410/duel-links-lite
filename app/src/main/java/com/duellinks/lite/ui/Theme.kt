package com.duellinks.lite.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DuelColors = darkColorScheme(
    primary = Color(0xFFF4B400),
    secondary = Color(0xFF1565C0),
    background = Color(0xFF0E1117),
    surface = Color(0xFF1A1F2B),
    onBackground = Color.White,
    onSurface = Color.White
)

@Composable
fun DuelTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = DuelColors, content = content)
}
