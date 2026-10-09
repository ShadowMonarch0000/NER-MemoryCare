package com.memorycare.ner.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF2B5C8F), // Calm professional medical/cognitive blue
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD6E4F0),
    onPrimaryContainer = Color(0xFF0A2E4E),
    secondary = Color(0xFF5B7065), // Calm sage green
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFE2EBE5),
    onSecondaryContainer = Color(0xFF18261F),
    tertiary = Color(0xFF7A5980),
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFF2E6F5),
    onTertiaryContainer = Color(0xFF2D1633),
    background = Color(0xFFF9FAFC), // Calm soft light background
    onBackground = Color(0xFF1A1C1E),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF1A1C1E),
    surfaceVariant = Color(0xFFDFE3EB),
    onSurfaceVariant = Color(0xFF42474E),
    outline = Color(0xFF72777F)
)

@Composable
fun NERMemoryCareTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        typography = Typography,
        content = content
    )
}
