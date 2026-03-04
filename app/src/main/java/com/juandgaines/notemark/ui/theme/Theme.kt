package com.juandgaines.notemark.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val NotemarkColorScheme = lightColorScheme(
    primary = BrandPrimary,
    onPrimary = SurfaceLowest,
    surface = Surface,
    onSurface = OnSurface,
    onSurfaceVariant = OnSurfaceVariant,
    background = Background,
    onBackground = OnSurface,
    error = ErrorRed,
    onError = SurfaceLowest,
    outline = OnSurface12,
    surfaceVariant = Surface,
    inverseSurface = OnSurface,
    inverseOnSurface = SurfaceLowest,
)

@Composable
fun NotemarkTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = NotemarkColorScheme,
        typography = Typography,
        content = content
    )
}
