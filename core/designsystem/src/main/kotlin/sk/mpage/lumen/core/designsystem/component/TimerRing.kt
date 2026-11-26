package sk.mpage.lumen.core.designsystem.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import sk.mpage.lumen.core.designsystem.preview.PreviewSurface
import sk.mpage.lumen.core.designsystem.theme.Lumora

@Composable
public fun TimerRing(
    remainingMs: () -> Long,
    totalMs: Long,
    warningAnnouncement: String,
    modifier: Modifier = Modifier,
    warningAtMs: Long = 5_000,
    criticalAtMs: Long = 3_000,
) {
    val seconds by remember { derivedStateOf { ((remainingMs() + 999) / 1_000).toInt() } }
    val level by remember {
        derivedStateOf {
            val left = remainingMs()
            when {
                left <= criticalAtMs -> 2
                left <= warningAtMs -> 1
                else -> 0
            }
        }
    }
    val color =
        when (level) {
            2 -> MaterialTheme.colorScheme.error
            1 -> Lumora.colors.warning
            else -> MaterialTheme.colorScheme.primary
        }
    val warned = level > 0
    Box(
        modifier =
            modifier.size(Lumora.sizes.lg).clearAndSetSemantics {
                contentDescription = if (warned) warningAnnouncement else ""
                liveRegion = LiveRegionMode.Polite
            },
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator(
            progress = { (remainingMs().toFloat() / totalMs).coerceIn(0f, 1f) },
            modifier = Modifier.size(Lumora.sizes.lg),
            color = color,
            strokeWidth = 6.dp,
            strokeCap = StrokeCap.Round,
            trackColor = MaterialTheme.colorScheme.surfaceContainerHighest,
        )
        Text(
            text = seconds.toString(),
            style = MaterialTheme.typography.titleMedium.copy(fontFeatureSettings = TABULAR_NUMBERS),
            color = color,
        )
    }
}

private const val TABULAR_NUMBERS = "tnum"

@PreviewLightDark
@Composable
private fun TimerRingPreview() {
    PreviewSurface {
        Row(Modifier.padding(Lumora.spacing.lg)) {
            listOf(12_000L, 4_200L, 1_500L).forEach { left ->
                TimerRing(
                    remainingMs = { left },
                    totalMs = 15_000,
                    warningAnnouncement = "5 seconds left",
                    modifier = Modifier.padding(Lumora.spacing.sm),
                )
            }
        }
    }
}
