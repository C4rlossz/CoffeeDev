package dev.coffeedev.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import dev.coffeedev.R
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

internal val Light = lightColorScheme(
    primary = Color(0xFF6B3F21), onPrimary = Color.White,
    secondary = Color(0xFF845324), onSecondary = Color.White,
    secondaryContainer = Color(0xFFEED9BE), onSecondaryContainer = Color(0xFF2B1D14),
    tertiary = Color(0xFF6B3F21), onTertiary = Color.White,
    tertiaryContainer = Color(0xFFEED9BE), onTertiaryContainer = Color(0xFF2B1D14),
    inverseSurface = Color(0xFF2B1D14), inverseOnSurface = Color(0xFFF7F3EC), inversePrimary = Color(0xFFE0AF76),
    surfaceTint = Color(0xFF6B3F21),
    background = Color(0xFFF7F3EC), onBackground = Color(0xFF2B1D14),
    surface = Color.White, onSurface = Color(0xFF2B1D14),
    surfaceVariant = Color(0xFFEDE4D8), onSurfaceVariant = Color(0xFF68574A),
    primaryContainer = Color(0xFFEED9BE), onPrimaryContainer = Color(0xFF2B1D14),
    outline = Color(0xFF776354), outlineVariant = Color(0xFF776354),
    error = Color(0xFFB3261E), onError = Color.White,
    errorContainer = Color(0xFFFFDAD6), onErrorContainer = Color(0xFF410002)
)
internal val Dark = darkColorScheme(
    primary = Color(0xFFE0AF76), onPrimary = Color(0xFF2B1D14),
    secondary = Color(0xFFE0AF76), onSecondary = Color(0xFF14110E),
    secondaryContainer = Color(0xFF483120), onSecondaryContainer = Color(0xFFF5EEE5),
    tertiary = Color(0xFFE0AF76), onTertiary = Color(0xFF14110E),
    tertiaryContainer = Color(0xFF483120), onTertiaryContainer = Color(0xFFF5EEE5),
    inverseSurface = Color(0xFFF5EEE5), inverseOnSurface = Color(0xFF241D17), inversePrimary = Color(0xFF6B3F21),
    surfaceTint = Color(0xFFE0AF76),
    background = Color(0xFF14110E), onBackground = Color(0xFFF5EEE5),
    surface = Color(0xFF241D17), onSurface = Color(0xFFF5EEE5),
    surfaceVariant = Color(0xFF382C22), onSurfaceVariant = Color(0xFFD3C2B0),
    primaryContainer = Color(0xFF483120), onPrimaryContainer = Color(0xFFF5EEE5),
    outline = Color(0xFFA38B76), outlineVariant = Color(0xFFA38B76),
    error = Color(0xFFFFB4AB), onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A), onErrorContainer = Color(0xFFFFDAD6)
)
// OFL-licensed fonts bundled with the app. No runtime font downloads.
private val Sora = FontFamily(Font(R.font.sora, FontWeight.Normal), Font(R.font.sora, FontWeight.SemiBold), Font(R.font.sora, FontWeight.Bold))
private val Inter = FontFamily(Font(R.font.inter, FontWeight.Normal), Font(R.font.inter, FontWeight.Medium), Font(R.font.inter, FontWeight.SemiBold))
private val Type = Typography(
    headlineLarge = TextStyle(fontFamily = Sora, fontWeight = FontWeight.Bold, fontSize = 34.sp, lineHeight = 39.sp, letterSpacing = (-1).sp),
    headlineMedium = TextStyle(fontFamily = Sora, fontWeight = FontWeight.Bold, fontSize = 27.sp, lineHeight = 33.sp, letterSpacing = (-0.7).sp),
    titleLarge = TextStyle(fontFamily = Sora, fontWeight = FontWeight.SemiBold, fontSize = 21.sp, lineHeight = 27.sp),
    titleMedium = TextStyle(fontFamily = Sora, fontWeight = FontWeight.SemiBold, fontSize = 16.sp, lineHeight = 22.sp),
    bodyLarge = TextStyle(fontFamily = Inter, fontSize = 16.sp, lineHeight = 24.sp),
    bodyMedium = TextStyle(fontFamily = Inter, fontSize = 14.sp, lineHeight = 21.sp),
    bodySmall = TextStyle(fontFamily = Inter, fontSize = 12.sp, lineHeight = 18.sp),
    labelLarge = TextStyle(fontFamily = Inter, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
)
@Composable fun CoffeeTheme(theme: String, content: @Composable () -> Unit) {
    val dark = theme == "dark" || (theme == "system" && isSystemInDarkTheme())
    val view = LocalView.current
    SideEffect {
        (view.context as? android.app.Activity)?.window?.let { window ->
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = !dark
                isAppearanceLightNavigationBars = !dark
            }
        }
    }
    MaterialTheme(colorScheme = if (dark) Dark else Light, typography = Type, content = content)
}
