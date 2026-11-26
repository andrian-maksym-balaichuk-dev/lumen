package sk.mpage.lumen.core.designsystem.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.dropShadow
import androidx.compose.ui.graphics.shadow.Shadow
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import sk.mpage.lumen.core.designsystem.preview.PreviewSurface
import sk.mpage.lumen.core.designsystem.theme.Lumora

@Composable
public fun LumoraPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    val scheme = MaterialTheme.colorScheme
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier =
            modifier
                .fillMaxWidth()
                .height(Lumora.sizes.lg)
                .dropShadow(
                    MaterialTheme.shapes.extraLarge,
                    Shadow(
                        radius = 24.dp,
                        color = scheme.primary.copy(alpha = if (enabled) 0.35f else 0f),
                        offset = DpOffset(0.dp, 8.dp),
                    ),
                ),
        shape = MaterialTheme.shapes.extraLarge,
        colors =
            ButtonDefaults.buttonColors(
                containerColor = scheme.primary,
                contentColor = scheme.onPrimary,
                disabledContainerColor = Lumora.glass.frost.tint,
            ),
        border = BorderStroke(1.dp, Lumora.glass.highlight),
    ) {
        Text(text, style = MaterialTheme.typography.titleMedium)
    }
}

@PreviewLightDark
@Composable
private fun LumoraPrimaryButtonPreview() {
    PreviewSurface {
        LumoraPrimaryButton(text = "Start", onClick = {}, modifier = Modifier.padding(Lumora.spacing.lg))
    }
}
