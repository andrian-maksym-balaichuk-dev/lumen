package sk.mpage.lumen.core.designsystem.component

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.tooling.preview.PreviewLightDark
import sk.mpage.lumen.core.designsystem.preview.PreviewSurface
import sk.mpage.lumen.core.designsystem.theme.Lumora

@Composable
public fun ProgressPill(
    progress: Float,
    label: String,
    description: String,
    modifier: Modifier = Modifier,
) {
    val animated by animateFloatAsState(progress, Lumora.motion.selection(), label = "quiz progress")
    GlassSurface(modifier = modifier.clearAndSetSemantics { contentDescription = description }) {
        Row(
            modifier = Modifier.padding(horizontal = Lumora.spacing.lg, vertical = Lumora.spacing.sm),
            horizontalArrangement = Arrangement.spacedBy(Lumora.spacing.md),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            LinearProgressIndicator(progress = { animated }, modifier = Modifier.width(Lumora.sizes.xxxl))
            Text(label, style = MaterialTheme.typography.labelLarge)
        }
    }
}

@PreviewLightDark
@Composable
private fun ProgressPillPreview() {
    PreviewSurface {
        ProgressPill(
            progress = 0.6f,
            label = "6 / 10",
            description = "Question 6 of 10",
            modifier = Modifier.padding(Lumora.spacing.lg),
        )
    }
}
