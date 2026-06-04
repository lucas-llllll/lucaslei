package com.lucaslei.app.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val Mint = Color(0xFF98D8C8)
val MintDark = Color(0xFF4DB6AC)
val MintLight = Color(0xFFE0F2F1)
val MintBg = Color(0xFFF1F8F6)

// Keep some accent colors from the original
val Orange = Color(0xFFF08030)
val Green = Color(0xFF67C23A)
val Red = Color(0xFFF56C6C)

private val LightColorScheme = lightColorScheme(
    primary = MintDark,
    onPrimary = Color.White,
    primaryContainer = MintLight,
    onPrimaryContainer = Color(0xFF1B4D43),
    secondary = Mint,
    onSecondary = Color.White,
    background = MintBg,
    onBackground = Color(0xFF1A1C1A),
    surface = Color.White,
    onSurface = Color(0xFF1A1C1A),
    surfaceVariant = Color(0xFFDBE5E1),
    onSurfaceVariant = Color(0xFF3F4945),
    outline = Color(0xFF6F7975),
    error = Red,
    onError = Color.White
)

@Composable
fun LucasleiTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        content = content
    )
}
