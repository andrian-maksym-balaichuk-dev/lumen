package sk.mpage.lumen.core.designsystem.component

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.currentStateAsState
import sk.mpage.lumen.core.designsystem.theme.Lumora
import sk.mpage.lumen.core.designsystem.theme.LumoraTheme
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.sin

private const val DRIFT_MILLIS = 24_000

@Composable
public fun AuraBackground(modifier: Modifier = Modifier) {
    val scheme = MaterialTheme.colorScheme
    val glass = Lumora.glass
    val dark = isSystemInDarkTheme()
    val lifecycleState by LocalLifecycleOwner.current.lifecycle.currentStateAsState()
    val drifting = glass.showAura && !Lumora.motion.isReduced && lifecycleState.isAtLeast(Lifecycle.State.STARTED)
    val phase = remember { Animatable(0f) }

    LaunchedEffect(drifting) {
        while (drifting) {
            val remaining = ((1f - phase.value) * DRIFT_MILLIS).toInt()
            phase.animateTo(1f, tween(remaining, easing = LinearEasing))
            phase.snapTo(0f)
        }
    }

    val background = scheme.background
    val blobs =
        if (glass.showAura) {
            listOf(
                Blob(scheme.primary.copy(alpha = if (dark) 0.30f else 0.22f), 0.15f, 0.12f, 0.75f, 0f),
                Blob(scheme.secondary.copy(alpha = if (dark) 0.22f else 0.16f), 0.90f, 0.48f, 0.65f, 1f / 3f),
                Blob(scheme.tertiary.copy(alpha = if (dark) 0.16f else 0.10f), 0.40f, 0.95f, 0.80f, 2f / 3f),
            )
        } else {
            emptyList()
        }

    Spacer(
        modifier =
            modifier
                .fillMaxSize()
                .clearAndSetSemantics {}
                .drawBehind {
                    drawRect(background)
                    val angle = phase.value * 2f * PI.toFloat()
                    blobs.forEach { drawBlob(it, angle) }
                },
    )
}

private class Blob(
    val color: Color,
    val x: Float,
    val y: Float,
    val radius: Float,
    val offset: Float,
)

private fun DrawScope.drawBlob(
    blob: Blob,
    angle: Float,
) {
    val turn = angle + blob.offset * 2f * PI.toFloat()
    val center =
        Offset(
            x = size.width * (blob.x + 0.06f * sin(turn)),
            y = size.height * (blob.y + 0.04f * cos(turn)),
        )
    val radius = max(size.width, size.height) * blob.radius * 0.6f
    drawCircle(
        brush = Brush.radialGradient(listOf(blob.color, blob.color.copy(alpha = 0f)), center, radius),
        radius = radius,
        center = center,
    )
}

@PreviewLightDark
@Composable
private fun AuraBackgroundPreview() {
    LumoraTheme { AuraBackground() }
}
