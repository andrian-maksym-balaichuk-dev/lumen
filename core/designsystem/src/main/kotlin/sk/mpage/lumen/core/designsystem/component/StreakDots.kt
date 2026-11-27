package sk.mpage.lumen.core.designsystem.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import sk.mpage.lumen.core.designsystem.icon.LumoraIcons
import sk.mpage.lumen.core.designsystem.preview.PreviewSurface
import sk.mpage.lumen.core.designsystem.theme.Lumora

public enum class StreakDotState { QUALIFIED, PENDING, MISSED }

@Immutable
public data class StreakDot(
    val label: String,
    val state: StreakDotState,
)

@Composable
public fun StreakDots(
    dots: List<StreakDot>,
    summary: String,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth().clearAndSetSemantics { contentDescription = summary },
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        dots.forEach { dot ->
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(Lumora.spacing.xs),
            ) {
                Text(
                    text = dot.label,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Dot(dot.state)
            }
        }
    }
}

@Composable
private fun Dot(state: StreakDotState) {
    val (container, border) =
        when (state) {
            StreakDotState.QUALIFIED -> Lumora.colors.streakFlame to null
            StreakDotState.PENDING -> Color.Transparent to BorderStroke(2.dp, Lumora.colors.streakFlame)
            StreakDotState.MISSED -> MaterialTheme.colorScheme.surfaceVariant to null
        }
    Surface(
        shape = CircleShape,
        color = container,
        contentColor = MaterialTheme.colorScheme.surface,
        border = border,
        modifier = Modifier.size(Lumora.sizes.sm),
    ) {
        if (state == StreakDotState.QUALIFIED) {
            Box(contentAlignment = Alignment.Center) {
                Icon(painterResource(LumoraIcons.Check), contentDescription = null, Modifier.size(Lumora.sizes.xxs))
            }
        }
    }
}

@PreviewLightDark
@Composable
private fun StreakDotsPreview() {
    PreviewSurface {
        StreakDots(
            dots =
                listOf("M", "T", "W", "T", "F", "S", "S").mapIndexed { index, label ->
                    val state =
                        when {
                            index == 6 -> StreakDotState.PENDING
                            index == 2 -> StreakDotState.MISSED
                            else -> StreakDotState.QUALIFIED
                        }
                    StreakDot(label, state)
                },
            summary = "Streak 3 of 7 days",
            modifier = Modifier.padding(Lumora.spacing.lg),
        )
    }
}
