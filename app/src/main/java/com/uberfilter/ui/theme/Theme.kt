package com.uberfilter.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColors = darkColorScheme(
    primary   = Color(0xFF1B8C3E),
    secondary = Color(0xFFE53935),
    background = Color(0xFF121212),
    surface    = Color(0xFF1E1E1E),
    onPrimary  = Color.White,
    onBackground = Color.White,
    onSurface  = Color.White
)

@Composable
fun UberFilterTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DarkColors,
        content = content
    )
}
