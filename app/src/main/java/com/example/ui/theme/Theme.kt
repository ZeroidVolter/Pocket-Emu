package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val CosmicColorScheme = darkColorScheme(
    primary = CyberCyan,
    onPrimary = SpaceBlack,
    secondary = HoloBlue,
    onSecondary = SpaceBlack,
    tertiary = CyberMagenta,
    onTertiary = SpaceBlack,
    background = SpaceBlack,
    onBackground = TerminalGray,
    surface = DarkTerminal,
    onSurface = TerminalGray,
    surfaceVariant = MediumTerminal,
    onSurfaceVariant = CyberCyan,
    error = CyberMagenta
)

@Composable
fun MyApplicationTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = CosmicColorScheme,
        typography = Typography,
        content = content
    )
}
