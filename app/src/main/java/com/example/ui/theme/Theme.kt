package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val ZenithColorScheme = darkColorScheme(
    primary = ZenithPrimaryNeon,
    onPrimary = ZenithBgDark,
    primaryContainer = ZenithPrimaryDeep,
    onPrimaryContainer = ZenithTextHigh,
    secondary = ZenithSecondaryViolet,
    onSecondary = ZenithBgDark,
    secondaryContainer = ZenithBgSurfaceElevated,
    onSecondaryContainer = ZenithTertiaryLavender,
    tertiary = ZenithTertiaryLavender,
    onTertiary = ZenithBgDark,
    background = ZenithBgDark,
    onBackground = ZenithTextHigh,
    surface = ZenithBgSurface,
    onSurface = ZenithTextHigh,
    surfaceVariant = ZenithBgSurfaceElevated,
    onSurfaceVariant = ZenithTextMedium,
    outline = ZenithGlowBorder,
    outlineVariant = ZenithBgCard,
    error = ZenithError,
    onError = ZenithBgDark
)

@Composable
fun ZenithTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = ZenithColorScheme,
        typography = ZenithTypography,
        content = content
    )
}

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    ZenithTheme(content = content)
}
