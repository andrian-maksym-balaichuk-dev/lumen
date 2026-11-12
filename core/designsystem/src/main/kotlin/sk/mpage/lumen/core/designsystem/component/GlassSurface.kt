package sk.mpage.lumen.core.designsystem.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.dropShadow
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.shadow.Shadow
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import dev.chrisbanes.haze.HazeInput
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.blur.HazeBlurStyle
import dev.chrisbanes.haze.blur.hazeBlur
import sk.mpage.lumen.core.designsystem.preview.PreviewSurface
import sk.mpage.lumen.core.designsystem.theme.Lumora
import sk.mpage.lumen.core.designsystem.tokens.GlassMaterial

public val LocalHazeState: ProvidableCompositionLocal<HazeState?> =
    staticCompositionLocalOf { null }

@Composable
public fun GlassSurface(
    modifier: Modifier = Modifier,
    material: GlassMaterial = Lumora.glass.regular,
    shape: Shape = CircleShape,
    content: @Composable () -> Unit,
) {
    val glass = Lumora.glass
    val hazeState = LocalHazeState.current
    val blur =
        if (glass.isEnabled && hazeState != null && material.blur > 0.dp) {
            val style = remember(material.blur) { HazeBlurStyle { blurRadius(material.blur) } }
            Modifier.hazeBlur(input = HazeInput.Sources(hazeState), style = style)
        } else {
            Modifier
        }
    Surface(
        modifier =
            modifier
                .dropShadow(shape, Shadow(radius = 24.dp, color = glass.shadow, offset = DpOffset(0.dp, 8.dp)))
                .clip(shape)
                .then(blur),
        shape = shape,
        color = material.tint,
        tonalElevation = 0.dp,
        border = BorderStroke(1.dp, glass.highlight),
        content = content,
    )
}

@PreviewLightDark
@Composable
private fun GlassSurfacePreview() {
    PreviewSurface {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            listOf("Frost" to Lumora.glass.frost, "Regular" to Lumora.glass.regular, "Thick" to Lumora.glass.thick)
                .forEach { (name, material) ->
                    GlassSurface(
                        modifier = Modifier.fillMaxWidth(),
                        material = material,
                        shape = MaterialTheme.shapes.large,
                    ) {
                        Text(name, modifier = Modifier.padding(16.dp), style = MaterialTheme.typography.titleMedium)
                    }
                }
        }
    }
}
