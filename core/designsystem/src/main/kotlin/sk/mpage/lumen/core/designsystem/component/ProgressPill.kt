package sk.mpage.lumen.core.designsystem.component

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.tooling.preview.PreviewLightDark
import sk.mpage.lumen.core.designsystem.icon.LumoraIcons
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
            LinearProgressIndicator(
                progress = { animated },
                modifier = Modifier.width(Lumora.sizes.xxxl),
                trackColor = MaterialTheme.colorScheme.primaryContainer,
                drawStopIndicator = {},
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge.copy(fontFeatureSettings = "tnum"),
                maxLines = 1,
                softWrap = false,
            )
        }
    }
}

@Composable
public fun MarathonProgressPill(
    count: Int,
    description: String,
    modifier: Modifier = Modifier,
) {
    GlassSurface(modifier = modifier.clearAndSetSemantics { contentDescription = description }) {
        Row(
            modifier = Modifier.padding(horizontal = Lumora.spacing.lg, vertical = Lumora.spacing.sm),
            horizontalArrangement = Arrangement.spacedBy(Lumora.spacing.sm),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                painter = painterResource(LumoraIcons.Flame),
                contentDescription = null,
                tint = Lumora.colors.streakFlame,
                modifier = Modifier.size(Lumora.sizes.xs),
            )
            AnimatedContent(
                targetState = count,
                transitionSpec = {
                    slideInVertically { it } + fadeIn() togetherWith
                        slideOutVertically { -it } + fadeOut()
                },
                label = "marathon count",
            ) { value ->
                Text(value.toString(), style = MaterialTheme.typography.labelLarge)
            }
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

@PreviewLightDark
@Composable
private fun MarathonProgressPillPreview() {
    PreviewSurface {
        MarathonProgressPill(count = 37, description = "37 in a row", modifier = Modifier.padding(Lumora.spacing.lg))
    }
}
