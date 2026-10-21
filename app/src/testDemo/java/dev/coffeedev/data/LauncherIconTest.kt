package dev.coffeedev.data

import android.graphics.drawable.AdaptiveIconDrawable
import dev.coffeedev.R
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class LauncherIconTest {
    @Test fun androidSelectsLayeredAdaptiveAndRoundIcons() {
        val context = RuntimeEnvironment.getApplication()
        val icon = context.getDrawable(R.mipmap.ic_launcher)
        val round = context.getDrawable(R.mipmap.ic_launcher_round)
        assertTrue(icon is AdaptiveIconDrawable)
        assertTrue(round is AdaptiveIconDrawable)
        assertNotNull((icon as AdaptiveIconDrawable).background)
        assertNotNull(icon.foreground)
    }
}
