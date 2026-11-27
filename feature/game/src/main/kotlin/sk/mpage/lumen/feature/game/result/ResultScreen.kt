package sk.mpage.lumen.feature.game.result

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.style.TextAlign
import sk.mpage.lumen.core.designsystem.component.LumoraPrimaryButton
import sk.mpage.lumen.core.designsystem.component.StatTile
import sk.mpage.lumen.core.designsystem.icon.LumoraIcons
import sk.mpage.lumen.core.designsystem.layout.LumoraFlex
import sk.mpage.lumen.core.designsystem.preview.LumoraPreviews
import sk.mpage.lumen.core.designsystem.preview.PreviewSurface
import sk.mpage.lumen.core.designsystem.theme.Lumora
import sk.mpage.lumen.core.model.Avatar
import sk.mpage.lumen.core.model.Category
import sk.mpage.lumen.core.model.Difficulty
import sk.mpage.lumen.core.model.GameMode
import sk.mpage.lumen.core.model.QuestionType
import sk.mpage.lumen.core.model.QuizConfig
import sk.mpage.lumen.core.ui.ProfileAvatar
import sk.mpage.lumen.core.ui.displayName
import sk.mpage.lumen.core.ui.formatDuration
import sk.mpage.lumen.core.ui.formatSeconds
import sk.mpage.lumen.feature.game.R

@Composable
internal fun ResultScreen(
    state: ResultUiState,
    onClose: () -> Unit,
    onPlayAgain: (QuizConfig) -> Unit,
    onShare: () -> Unit,
    onReview: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier.fillMaxSize().safeDrawingPadding()) {
        when (state) {
            ResultUiState.Loading -> {
                CircularProgressIndicator(Modifier.align(Alignment.Center))
            }

            ResultUiState.NotFound -> {
                Text(
                    text = stringResource(R.string.result_not_found),
                    modifier = Modifier.align(Alignment.Center).padding(Lumora.spacing.gutterCompact),
                    style = MaterialTheme.typography.bodyLarge,
                )
            }

            is ResultUiState.Content -> {
                ResultContent(state, onPlayAgain, onShare, onReview)
            }
        }
        IconButton(onClick = onClose, modifier = Modifier.align(Alignment.TopEnd).padding(Lumora.spacing.sm)) {
            Icon(painterResource(LumoraIcons.Close), contentDescription = stringResource(R.string.result_close))
        }
    }
}

@Composable
private fun ResultContent(
    state: ResultUiState.Content,
    onPlayAgain: (QuizConfig) -> Unit,
    onShare: () -> Unit,
    onReview: () -> Unit,
) {
    LumoraFlex(
        items = listOf({ ScoreBlock(state) }, { Stats(state, onPlayAgain, onShare, onReview) }),
        modifier =
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = Lumora.spacing.gutterCompact, vertical = Lumora.spacing.huge),
        itemAlignment = Alignment.CenterVertically,
    )
}

@Composable
private fun ScoreBlock(state: ResultUiState.Content) {
    val description =
        stringResource(
            R.string.result_score_description,
            state.correct,
            state.total,
            state.wrong,
            state.timeouts,
            state.percent,
            formatDuration(state.durationSeconds),
        )
    Column(
        modifier = Modifier.fillMaxWidth().clearAndSetSemantics { contentDescription = description },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(Lumora.spacing.sm),
    ) {
        ProfileAvatar(avatar = state.avatar, contentDescription = null, size = Lumora.sizes.xxl)
        Text(
            text =
                if (state.config.mode == GameMode.MARATHON) {
                    state.correct.toString()
                } else {
                    stringResource(R.string.result_score, state.correct, state.total)
                },
            style = MaterialTheme.typography.displayLarge,
            color = MaterialTheme.colorScheme.onBackground,
        )
        if (state.config.mode == GameMode.MARATHON) {
            Text(
                text = pluralStringResource(R.plurals.result_in_a_row, state.correct, state.correct),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        if (state.answeredEveryQuestion) {
            Text(
                text = stringResource(R.string.result_every_question),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center,
            )
        }
        Text(
            text =
                stringResource(
                    if (state.percent >= GOOD_RUN_PERCENT) R.string.result_title_great else R.string.result_title_done,
                    state.playerName,
                ),
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun Stats(
    state: ResultUiState.Content,
    onPlayAgain: (QuizConfig) -> Unit,
    onShare: () -> Unit,
    onReview: () -> Unit,
) {
    val locale = LocalConfiguration.current.locales[0]
    Column(verticalArrangement = Arrangement.spacedBy(Lumora.spacing.sm)) {
        Row(horizontalArrangement = Arrangement.spacedBy(Lumora.spacing.sm)) {
            StatTile(stringResource(R.string.result_correct), state.correct.toString(), Modifier.weight(1f))
            StatTile(
                label = stringResource(R.string.result_wrong),
                value = state.wrong.toString(),
                supporting =
                    if (state.timeouts > 0) {
                        pluralStringResource(R.plurals.result_timed_out, state.timeouts, state.timeouts)
                    } else {
                        null
                    },
                modifier = Modifier.weight(1f),
            )
            StatTile(stringResource(R.string.result_accuracy), "${state.percent} %", Modifier.weight(1f))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(Lumora.spacing.sm)) {
            StatTile(
                stringResource(R.string.result_total_time),
                formatDuration(state.durationSeconds),
                Modifier.weight(1f),
            )
            StatTile(
                stringResource(R.string.result_average_time),
                stringResource(R.string.result_seconds, formatSeconds(state.averageAnswerMs, locale)),
                Modifier.weight(1f),
            )
        }
        Text(
            text = configLabel(state.config),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(vertical = Lumora.spacing.sm),
        )
        LumoraPrimaryButton(text = stringResource(R.string.result_share), onClick = onShare)
        Row(horizontalArrangement = Arrangement.spacedBy(Lumora.spacing.sm)) {
            FilledTonalButton(onClick = onReview, modifier = Modifier.weight(1f).height(Lumora.sizes.lg)) {
                Text(stringResource(R.string.result_review))
            }
            FilledTonalButton(
                onClick = { onPlayAgain(state.config) },
                modifier = Modifier.weight(1f).height(Lumora.sizes.lg),
            ) {
                Text(stringResource(R.string.result_play_again))
            }
        }
    }
}

@Composable
private fun configLabel(config: QuizConfig): String {
    val resources = LocalResources.current
    val category =
        config.categoryId?.let { Category(it, config.categoryName).displayName(resources) }
            ?: stringResource(R.string.result_any_category)
    val difficulty =
        when (config.difficulty) {
            Difficulty.ANY -> stringResource(R.string.result_any_difficulty)
            Difficulty.EASY -> stringResource(R.string.game_difficulty_easy)
            Difficulty.MEDIUM -> stringResource(R.string.game_difficulty_medium)
            Difficulty.HARD -> stringResource(R.string.game_difficulty_hard)
        }
    val mode =
        when (config.mode) {
            GameMode.CLASSIC -> stringResource(R.string.result_mode_classic)
            GameMode.MARATHON -> stringResource(R.string.result_mode_marathon)
        }
    return "$category · $difficulty · $mode"
}

private const val GOOD_RUN_PERCENT = 80

@LumoraPreviews
@Composable
private fun ResultPreview() {
    PreviewSurface {
        ResultScreen(
            state =
                ResultUiState.Content(
                    playerName = "Ada",
                    avatar = Avatar.Preset("owl"),
                    answeredEveryQuestion = false,
                    config =
                        QuizConfig(
                            GameMode.CLASSIC,
                            17,
                            "Science & Nature",
                            Difficulty.MEDIUM,
                            QuestionType.MULTIPLE_CHOICE,
                        ),
                    correct = 8,
                    total = 10,
                    wrong = 2,
                    timeouts = 1,
                    percent = 80,
                    durationSeconds = 130,
                    averageAnswerMs = 10_200,
                ),
            onClose = {},
            onPlayAgain = {},
            onShare = {},
            onReview = {},
        )
    }
}
