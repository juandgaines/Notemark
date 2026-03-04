package com.juandgaines.notemark.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColorScheme = lightColorScheme(
    primary = Primary,
    onPrimary = OnPrimary,
    onSurface = OnSurface,
    onSurfaceVariant = OnSurfaceVariant,
    surface = Surface,
    background = SurfaceLowest,
    error = Error,
)

@Composable
fun NoteMarkTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        typography = Typography,
        content = content,
    )
}
