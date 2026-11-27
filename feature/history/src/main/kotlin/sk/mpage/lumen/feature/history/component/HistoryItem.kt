package sk.mpage.lumen.feature.history.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import sk.mpage.lumen.core.designsystem.icon.LumoraIcons
import sk.mpage.lumen.core.designsystem.theme.Lumora
import sk.mpage.lumen.core.model.Category
import sk.mpage.lumen.core.model.Difficulty
import sk.mpage.lumen.core.model.GameMode
import sk.mpage.lumen.core.model.QuizConfig
import sk.mpage.lumen.core.model.QuizSession
import sk.mpage.lumen.core.ui.categoryIcon
import sk.mpage.lumen.core.ui.displayName
import sk.mpage.lumen.core.ui.formatDuration
import sk.mpage.lumen.feature.history.R
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

@Composable
internal fun HistoryItem(
    session: QuizSession,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val config = session.config
    val time =
        DateTimeFormatter
            .ofLocalizedTime(FormatStyle.SHORT)
            .format(session.finishedAt.atZone(ZoneId.systemDefault()))
    Card(
        onClick = onClick,
        modifier = modifier,
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = Lumora.glass.frost.tint),
    ) {
        ListItem(
            colors = ListItemDefaults.colors(containerColor = Color.Transparent),
            leadingContent = {
                Icon(
                    painter = painterResource(categoryIcon(config.categoryId)),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                )
            },
            headlineContent = { Text(sessionTitle(config)) },
            supportingContent = {
                Text("${modeLabel(config.mode)} · ${formatDuration(session.durationSeconds)} · $time")
            },
            trailingContent = { Score(session) },
        )
    }
}

@Composable
private fun Score(session: QuizSession) {
    if (session.config.mode == GameMode.MARATHON) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                painter = painterResource(LumoraIcons.Flame),
                contentDescription = null,
                tint = Lumora.colors.streakFlame,
                modifier = Modifier.size(Lumora.sizes.xs),
            )
            Text(session.correctCount.toString(), style = MaterialTheme.typography.titleMedium)
        }
    } else {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.size(Lumora.sizes.md)) {
            CircularProgressIndicator(
                progress = { session.scorePercent / 100f },
                modifier = Modifier.size(Lumora.sizes.md),
                trackColor = MaterialTheme.colorScheme.primaryContainer,
            )
            Text(
                text = stringResource(R.string.history_score, session.correctCount, session.totalQuestions),
                style = MaterialTheme.typography.labelSmall,
            )
        }
    }
}

@Composable
internal fun sessionTitle(config: QuizConfig): String =
    "${categoryLabel(config)} · ${difficultyLabel(config.difficulty)}"

@Composable
internal fun categoryLabel(config: QuizConfig): String {
    val resources = LocalResources.current
    return config.categoryId?.let { Category(it, config.categoryName).displayName(resources) }
        ?: stringResource(R.string.history_any_category)
}

@Composable
internal fun difficultyLabel(difficulty: Difficulty): String =
    stringResource(
        when (difficulty) {
            Difficulty.ANY -> R.string.history_difficulty_any
            Difficulty.EASY -> R.string.history_difficulty_easy
            Difficulty.MEDIUM -> R.string.history_difficulty_medium
            Difficulty.HARD -> R.string.history_difficulty_hard
        },
    )

@Composable
internal fun modeLabel(mode: GameMode): String =
    stringResource(if (mode == GameMode.CLASSIC) R.string.history_mode_classic else R.string.history_mode_marathon)
