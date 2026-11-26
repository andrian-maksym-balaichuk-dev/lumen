package sk.mpage.lumen.core.designsystem.modifier

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.ContentDrawScope
import androidx.compose.ui.node.DrawModifierNode
import androidx.compose.ui.node.ModifierNodeElement
import androidx.compose.ui.platform.InspectorInfo
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

public fun Modifier.shimmer(
    base: Color,
    highlight: Color,
    animate: Boolean = true,
): Modifier = this then ShimmerElement(base, highlight, animate)

private data class ShimmerElement(
    val base: Color,
    val highlight: Color,
    val animate: Boolean,
) : ModifierNodeElement<ShimmerNode>() {
    override fun create(): ShimmerNode = ShimmerNode(base, highlight, animate)

    override fun update(node: ShimmerNode) {
        node.base = base
        node.highlight = highlight
        node.setAnimate(animate)
    }

    override fun InspectorInfo.inspectableProperties() {
        name = "shimmer"
        properties["base"] = base
        properties["highlight"] = highlight
        properties["animate"] = animate
    }
}

private class ShimmerNode(
    var base: Color,
    var highlight: Color,
    private var animate: Boolean,
) : Modifier.Node(),
    DrawModifierNode {
    private val progress = Animatable(0f)
    private var job: Job? = null

    override fun onAttach() {
        if (animate) start()
    }

    override fun onDetach() {
        job = null
    }

    fun setAnimate(value: Boolean) {
        if (animate == value) return
        animate = value
        if (value) {
            start()
        } else {
            job?.cancel()
            job = null
        }
    }

    private fun start() {
        if (!isAttached || job != null) return
        job =
            coroutineScope.launch {
                progress.snapTo(0f)
                progress.animateTo(
                    targetValue = 1f,
                    animationSpec = infiniteRepeatable(tween(SWEEP_MS, easing = LinearEasing), RepeatMode.Restart),
                )
            }
    }

    override fun ContentDrawScope.draw() {
        drawRect(base)
        if (animate) {
            val band = size.width
            val start = -band + progress.value * (size.width + band)
            drawRect(
                Brush.linearGradient(
                    colors = listOf(Color.Transparent, highlight, Color.Transparent),
                    start = Offset(start, 0f),
                    end = Offset(start + band, 0f),
                ),
            )
        }
    }

    private companion object {
        const val SWEEP_MS = 1_200
    }
}
