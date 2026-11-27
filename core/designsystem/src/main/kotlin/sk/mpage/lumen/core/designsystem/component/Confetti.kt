package sk.mpage.lumen.core.designsystem.component

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.tooling.preview.Preview
import sk.mpage.lumen.core.designsystem.preview.PreviewSurface
import sk.mpage.lumen.core.designsystem.theme.Lumora
import kotlin.random.Random

@Composable
public fun Confetti(
    modifier: Modifier = Modifier,
    onFinished: () -> Unit = {},
) {
    if (Lumora.motion.isReduced) {
        LaunchedEffect(Unit) { onFinished() }
        return
    }
    val colors =
        listOf(
            MaterialTheme.colorScheme.primary,
            MaterialTheme.colorScheme.tertiary,
            Lumora.colors.streakFlame,
            Lumora.colors.success,
        )
    val particles = remember { List(PARTICLE_COUNT) { Particle.random(Random(it)) } }
    val progress = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        progress.animateTo(1f, tween(DURATION_MS, easing = LinearEasing))
        onFinished()
    }
    Canvas(modifier.fillMaxSize()) {
        val t = progress.value
        particles.forEach { particle ->
            val x = size.width * (particle.startX + particle.drift * t)
            val y = size.height * (-0.05f + particle.speed * t + GRAVITY * t * t)
            val alpha = (1f - t).coerceIn(0f, 1f)
            rotate(degrees = particle.spin * t * 360f, pivot = Offset(x, y)) {
                drawRect(
                    color = colors[particle.colorIndex % colors.size].copy(alpha = alpha),
                    topLeft = Offset(x, y),
                    size = Size(particle.width * density, particle.height * density),
                )
            }
        }
    }
}

private data class Particle(
    val startX: Float,
    val drift: Float,
    val speed: Float,
    val spin: Float,
    val width: Float,
    val height: Float,
    val colorIndex: Int,
) {
    companion object {
        fun random(random: Random) =
            Particle(
                startX = random.nextFloat(),
                drift = random.nextFloat() * 0.4f - 0.2f,
                speed = 0.3f + random.nextFloat() * 0.5f,
                spin = random.nextFloat() * 4f - 2f,
                width = 6f + random.nextFloat() * 6f,
                height = 10f + random.nextFloat() * 8f,
                colorIndex = random.nextInt(4),
            )
    }
}

private const val PARTICLE_COUNT = 80
private const val DURATION_MS = 1_500
private const val GRAVITY = 0.5f

@Preview
@Composable
private fun ConfettiPreview() {
    PreviewSurface { Confetti() }
}
