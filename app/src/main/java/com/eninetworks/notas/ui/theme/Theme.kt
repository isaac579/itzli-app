package com.eninetworks.notas.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColors = darkColorScheme(
    primary = PrimaryDark,
    primaryContainer = PrimaryContainerDark,
    secondary = Secondary,
    tertiary = Tertiary,
    background = BackgroundDark,
    surface = BackgroundDark,
    error = Error
)

private val LightColors = lightColorScheme(
    primary = Primary,
    primaryContainer = PrimaryContainer,
    secondary = Secondary,
    tertiary = Tertiary,
    error = Error
)

@Composable
fun ItzliTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val ctx = LocalContext.current
        if (darkTheme) dynamicDarkColorScheme(ctx) else dynamicLightColorScheme(ctx)
    } else {
        if (darkTheme) DarkColors else LightColors
    }
    MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
