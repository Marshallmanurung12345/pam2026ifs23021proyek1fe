package org.delcom.pam_2026_ifs23021_proyek1_fe.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// ─── Warna Tema Baru: Hijau Teal Modern ───────────────────────────────────────

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF00796B),           // Teal 700
    onPrimary = Color.White,
    primaryContainer = Color(0xFF4DB6AC),  // Teal 300
    onPrimaryContainer = Color(0xFF004D40),// Teal 900
    secondary = Color(0xFF0097A7),         // Cyan 700
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFB2EBF2),// Cyan 100
    onSecondaryContainer = Color(0xFF006064),
    tertiary = Color(0xFF558B2F),          // Light Green 800
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFDCEDC8),
    onTertiaryContainer = Color(0xFF33691E),
    background = Color(0xFFF1F8F6),
    onBackground = Color(0xFF1A1A1A),
    surface = Color.White,
    onSurface = Color(0xFF1A1A1A),
    surfaceVariant = Color(0xFFE0F2F1),    // Teal 50
    onSurfaceVariant = Color(0xFF37474F),
    error = Color(0xFFD32F2F),
    onError = Color.White,
    errorContainer = Color(0xFFFFCDD2),
    onErrorContainer = Color(0xFFB71C1C),
    outline = Color(0xFF80CBC4),           // Teal 200
    outlineVariant = Color(0xFFE0F2F1)
)

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF80CBC4),           // Teal 200
    onPrimary = Color(0xFF003731),
    primaryContainer = Color(0xFF00695C),  // Teal 800
    onPrimaryContainer = Color(0xFFB2DFDB),// Teal 100
    secondary = Color(0xFF80DEEA),         // Cyan 200
    onSecondary = Color(0xFF004D40),
    secondaryContainer = Color(0xFF00838F),
    onSecondaryContainer = Color(0xFFB2EBF2),
    tertiary = Color(0xFFAED581),          // Light Green 300
    onTertiary = Color(0xFF1B5E20),
    tertiaryContainer = Color(0xFF33691E),
    onTertiaryContainer = Color(0xFFDCEDC8),
    background = Color(0xFF0F1F1D),
    onBackground = Color(0xFFE0F2F1),
    surface = Color(0xFF1A2E2C),
    onSurface = Color(0xFFE0F2F1),
    surfaceVariant = Color(0xFF1F3835),
    onSurfaceVariant = Color(0xFF80CBC4),
    error = Color(0xFFEF9A9A),
    onError = Color(0xFF7F0000),
    errorContainer = Color(0xFFB71C1C),
    onErrorContainer = Color(0xFFFFCDD2),
    outline = Color(0xFF00695C),
    outlineVariant = Color(0xFF1F3835)
)

@Composable
fun Pam2026ifs23021proyek1feTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val view = LocalView.current

    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primaryContainer.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography(),
        content = content
    )
}