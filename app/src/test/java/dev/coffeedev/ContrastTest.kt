package dev.coffeedev

import androidx.compose.material3.ColorScheme
import androidx.compose.ui.graphics.Color
import dev.coffeedev.ui.Light
import dev.coffeedev.ui.Dark
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.pow

class ContrastTest {
    private fun luminance(c: Color): Double {
        fun linear(v: Float): Double = if(v <= .04045f) v / 12.92 else ((v + .055) / 1.055).pow(2.4)
        return .2126*linear(c.red)+.7152*linear(c.green)+.0722*linear(c.blue)
    }
    private fun ratio(a: Color,b: Color): Double { val x=luminance(a);val y=luminance(b);return (maxOf(x,y)+.05)/(minOf(x,y)+.05) }
    private fun validate(s: ColorScheme) {
        listOf(s.onPrimary to s.primary,s.onSecondary to s.secondary,s.onTertiary to s.tertiary,
            s.onPrimaryContainer to s.primaryContainer,s.onSecondaryContainer to s.secondaryContainer,s.onTertiaryContainer to s.tertiaryContainer,
            s.onBackground to s.background,s.onSurface to s.surface,s.onSurfaceVariant to s.surfaceVariant,
            s.onError to s.error,s.onErrorContainer to s.errorContainer,s.inverseOnSurface to s.inverseSurface,
            s.primary to s.surface,s.primary to s.background,s.primary to s.primaryContainer,
            s.onSurfaceVariant to s.surface,s.onSurfaceVariant to s.background).forEach { (a,b) -> assertTrue("Text contrast ${ratio(a,b)}",ratio(a,b)>=4.5) }
        listOf(s.surface,s.background,s.primaryContainer,s.surfaceVariant).forEach { assertTrue("Outline contrast ${ratio(s.outline,it)}",ratio(s.outline,it)>=3) }
    }
    @Test fun lightThemeContrast() = validate(Light)
    @Test fun darkThemeContrast() = validate(Dark)
}
