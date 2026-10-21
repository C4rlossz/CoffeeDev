package dev.coffeedev.ui

import dev.coffeedev.R
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.layout.ContentScale
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.cos
import kotlin.math.sin

@Composable fun BrandMark(modifier: Modifier = Modifier.size(44.dp)) {
    val ink = MaterialTheme.colorScheme.primary
    Canvas(modifier) {
        val u = size.minDimension / 100f
        val p = Path().apply { moveTo(16*u, 44*u); lineTo(76*u, 44*u); cubicTo(76*u, 84*u, 16*u, 84*u, 16*u, 44*u) }
        drawPath(p, ink, style = Stroke(6*u))
        drawArc(ink, -100f, 225f, false, Offset(66*u, 46*u), Size(27*u, 25*u), style = Stroke(6*u))
        drawLine(ink, Offset(18*u, 85*u), Offset(74*u, 85*u), 5*u)
        val code = Path().apply { moveTo(34*u, 12*u); lineTo(25*u, 22*u); lineTo(34*u, 31*u); moveTo(53*u, 8*u); lineTo(43*u, 33*u); moveTo(63*u, 12*u); lineTo(73*u, 22*u); lineTo(63*u, 31*u) }
        drawPath(code, ink, style = Stroke(5*u))
    }
}

/** Bundled product photography: works offline, including cart and detail views. */
@Composable fun CoffeeArt(id: String, modifier: Modifier, description: String) {
    val image = when (id) {
        "cappuccino" -> R.drawable.photo_cappuccino
        "espresso" -> R.drawable.photo_espresso
        "latte" -> R.drawable.photo_latte
        "coldbrew" -> R.drawable.photo_coldbrew
        "mocha" -> R.drawable.photo_mocha
        "cookie" -> R.drawable.photo_cookie
        "croissant" -> R.drawable.photo_croissant
        "paochapa" -> R.drawable.photo_paochapa
        "paoqueijo" -> R.drawable.photo_paoqueijo
        "brownie" -> R.drawable.photo_brownie
        else -> R.drawable.photo_cappuccino
    }
    Image(painter = painterResource(image), contentDescription = description,
        contentScale = ContentScale.Crop, modifier = modifier)
}
