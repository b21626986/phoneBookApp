package com.example.phonebookapp.ui.theme

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Contacts (Mavi) Ekranı için Ana Tema Renkleri
private val LightColorSchemeContacts = lightColorScheme(
    primary = FigmaPrimaryBlue,
    onPrimary = FigmaWhite,
    primaryContainer = FigmaPrimaryBlueContainer,
    onPrimaryContainer = FigmaGray_950,

    secondary = FigmaSecondaryBlue,
    onSecondary = FigmaWhite,

    tertiary = FigmaSuccessGreen, // Yeşil, cihaza kaydetme ikonu için kullanıldı
    onTertiary = FigmaWhite,

    error = FigmaRedError,
    onError = FigmaWhite,

    background = FigmaGray_50,
    onBackground = FigmaGray_950,

    surface = FigmaWhite, // Kartlar, Listeler için
    onSurface = FigmaGray_950, // Ana yazı
    surfaceVariant = FigmaGray_50,
    onSurfaceVariant = FigmaGray_300, // İkincil yazı
    outline = FigmaGray_200 // Dividerlar, Çerçeveler
)


@Composable
fun PhoneBookAppTheme(
    content: @Composable () -> Unit
) {
    // Uygulamanın varsayılan teması Contacts (Mavi) temasını kullanır.
    val colorScheme = LightColorSchemeContacts

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography, // Varsayılan Typography'yi kullanır
        content = content
    )
}

/**
 * ProfileScreen ve diğer ekranlar için yerel tema geçersiz kılma yardımcı fonksiyonu.
 * Contacts ekranı dışındaki ekranlar için Ana (Primary) rengi değiştirir.
 */
@Composable
fun ThemedScreen(
    primaryColor: Color,
    content: @Composable () -> Unit
) {
    // Mevcut (Global) MaterialTheme renklerini kopyalar
    val currentColors = MaterialTheme.colorScheme

    // YENİ: Sadece Primary rengi değiştirir, diğer renkleri korur
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