package com.example.phonebookapp.ui.theme

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorSchemeContacts = lightColorScheme(
    primary = FigmaPrimaryBlue,
    onPrimary = FigmaWhite,
    primaryContainer = FigmaPrimaryBlueContainer,
    onPrimaryContainer = FigmaGray_950,

    secondary = FigmaSecondaryBlue,
    onSecondary = FigmaWhite,

    tertiary = FigmaSuccessGreen,
    onTertiary = FigmaWhite,

    error = FigmaRedError,
    onError = FigmaWhite,

    background = FigmaGray_50,
    onBackground = FigmaGray_950,

    surface = FigmaWhite,
    onSurface = FigmaGray_950,
    surfaceVariant = FigmaGray_50,
    onSurfaceVariant = FigmaGray_300,
    outline = FigmaGray_200
)


@Composable
fun PhoneBookAppTheme(
    content: @Composable () -> Unit
) {
    val colorScheme = LightColorSchemeContacts

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

@Composable
fun ThemedScreen(
    primaryColor: Color,
    content: @Composable () -> Unit
) {
    val currentColors = MaterialTheme.colorScheme

    val screenColorScheme = currentColors.copy(
        primary = primaryColor,
        primaryContainer = if (primaryColor == FigmaSuccessGreen) currentColors.surfaceVariant else FigmaPrimaryBlueContainer,
        onPrimaryContainer = currentColors.onSurface
    )

    MaterialTheme(
        colorScheme = screenColorScheme,
        typography = MaterialTheme.typography,
        content = content
    )
}