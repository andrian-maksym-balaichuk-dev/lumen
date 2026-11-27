package sk.mpage.lumen.core.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.PreviewLightDark
import sk.mpage.lumen.core.designsystem.component.StreakDot
import sk.mpage.lumen.core.designsystem.component.StreakDotState
import sk.mpage.lumen.core.designsystem.component.StreakDots
import sk.mpage.lumen.core.designsystem.icon.LumoraIcons
import sk.mpage.lumen.core.designsystem.preview.PreviewSurface
import sk.mpage.lumen.core.designsystem.theme.Lumora
import sk.mpage.lumen.core.model.StreakDay
import sk.mpage.lumen.core.model.StreakDayStatus
import sk.mpage.lumen.core.model.StreakState
import java.time.LocalDate
import java.time.format.TextStyle

@Composable
public fun StreakCard(
    streak: StreakState,
    modifier: Modifier = Modifier,
) {
    val locale = LocalConfiguration.current.locales[0]
    Card(
        modifier = modifier.fillMaxWidth(),
        colors =
            CardDefaults.cardColors(
                containerColor = Lumora.glass.frost.tint,
                contentColor = MaterialTheme.colorScheme.onSurface,
            ),
        shape = MaterialTheme.shapes.large,
    ) {
        Column(
            modifier = Modifier.padding(Lumora.spacing.cardPadding),
            verticalArrangement = Arrangement.spacedBy(Lumora.spacing.md),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = stringResource(R.string.streak_title),
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f).semantics { heading() },
                )
                Icon(
                    painter = painterResource(LumoraIcons.Flame),
                    contentDescription = null,
                    tint = Lumora.colors.streakFlame,
                    modifier = Modifier.size(Lumora.sizes.xs),
                )
                Text(
                    text = pluralStringResource(R.plurals.streak_days, streak.current, streak.current),
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(start = Lumora.spacing.xs),
                )
            }
            StreakDots(
                dots =
                    streak.lastSevenDays.map { day ->
                        StreakDot(day.date.dayOfWeek.getDisplayName(TextStyle.NARROW, locale), day.status.toDotState())
                    },
                summary =
                    stringResource(
                        R.string.streak_summary,
                        streak.challengeProgress,
                        stringResource(
                            if (streak.playedToday) R.string.streak_today_played else R.string.streak_today_not_played,
                        ),
                    ),
            )
            Text(
                text =
                    stringResource(
                        when {
                            streak.playedToday -> R.string.streak_hint_done
                            streak.atRisk -> R.string.streak_hint_at_risk
                            else -> R.string.streak_hint_pending
                        },
                    ),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

private fun StreakDayStatus.toDotState(): StreakDotState =
    when (this) {
        StreakDayStatus.QUALIFIED -> StreakDotState.QUALIFIED
        StreakDayStatus.MISSED -> StreakDotState.MISSED
        StreakDayStatus.TODAY_PENDING -> StreakDotState.PENDING
    }

@PreviewLightDark
@Composable
private fun StreakCardPreview() {
    val today = LocalDate.of(2026, 11, 25)
    PreviewSurface {
        StreakCard(
            streak =
                StreakState(
                    current = 4,
                    best = 9,
                    playedToday = false,
                    atRisk = true,
                    challengeProgress = 4,
                    challengeCompleted = false,
                    challengesCompleted = 1,
                    lastSevenDays =
                        List(7) { index ->
                            val status =
                                when (index) {
                                    6 -> StreakDayStatus.TODAY_PENDING
                                    0, 1 -> StreakDayStatus.MISSED
                                    else -> StreakDayStatus.QUALIFIED
                                }
                            StreakDay(today.minusDays(6L - index), status)
                        },
                ),
            modifier = Modifier.padding(Lumora.spacing.lg),
        )
    }
}
