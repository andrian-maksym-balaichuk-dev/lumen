package sk.mpage.lumen.feature.game.component

import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import sk.mpage.lumen.core.designsystem.component.AnswerOptionCard
import sk.mpage.lumen.core.designsystem.component.AnswerState
import sk.mpage.lumen.core.designsystem.layout.LumoraFlex
import sk.mpage.lumen.core.designsystem.theme.Lumora
import sk.mpage.lumen.core.model.Difficulty
import sk.mpage.lumen.core.model.QuestionType
import sk.mpage.lumen.feature.game.R
import sk.mpage.lumen.feature.game.play.Feedback
import sk.mpage.lumen.feature.game.play.GameUiState
import sk.mpage.lumen.feature.game.play.OptionUi

@Composable
internal fun QuestionContent(
    state: GameUiState.Playing,
    onOptionTapped: (String) -> Unit,
    onShown: () -> Unit,
    onNext: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val motion = Lumora.motion
    val currentOnShown by rememberUpdatedState(onShown)
    LaunchedEffect(state.presentationId) {
        if (!motion.isReduced) delay(motion.optionStaggerMs.toLong() * (state.options.size - 1) + ENTER_SETTLE_MS)
        withFrameNanos {}
        currentOnShown()
    }
    LumoraFlex(
        items =
            listOf(
                { QuestionText(state) },
                { Options(state, onOptionTapped, onNext) },
            ),
        modifier =
            modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = Lumora.spacing.gutterCompact, vertical = Lumora.spacing.lg),
    )
}

@Composable
private fun QuestionText(state: GameUiState.Playing) {
    Column(verticalArrangement = Arrangement.spacedBy(Lumora.spacing.sm)) {
        Text(
            text = "${state.categoryName.substringAfter(": ")} · ${difficultyLabel(state.difficulty)}".uppercase(),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = state.text,
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.semantics { heading() },
        )
    }
}

@Composable
private fun Options(
    state: GameUiState.Playing,
    onOptionTapped: (String) -> Unit,
    onNext: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(Lumora.spacing.md)) {
        if (state.type == QuestionType.TRUE_FALSE) {
            Row(horizontalArrangement = Arrangement.spacedBy(Lumora.spacing.md)) {
                state.options.forEachIndexed { index, option ->
                    Option(state, index, option, onOptionTapped, Modifier.weight(1f), tall = true)
                }
            }
        } else {
            state.options.forEachIndexed { index, option ->
                Option(state, index, option, onOptionTapped, Modifier, tall = false)
            }
        }
        state.feedback?.let { FeedbackRow(it, onNext) }
    }
}

@Composable
private fun Option(
    state: GameUiState.Playing,
    index: Int,
    option: OptionUi,
    onOptionTapped: (String) -> Unit,
    modifier: Modifier,
    tall: Boolean,
) {
    val motion = Lumora.motion
    val entrance = remember(state.presentationId) { Animatable(if (motion.isReduced) 1f else 0f) }
    LaunchedEffect(state.presentationId) {
        delay(motion.optionStaggerMs.toLong() * index)
        entrance.animateTo(1f, motion.optionEnter())
    }
    val letter = ('A' + index).toString()
    AnswerOptionCard(
        letter = letter,
        text = option.text,
        state = option.state,
        onClick = { onOptionTapped(option.text) },
        enabled = state.acceptsAnswers,
        description = stringResource(R.string.game_option_description, letter, option.text),
        stateDescription =
            when (option.state) {
                AnswerState.CORRECT -> stringResource(R.string.game_option_correct)
                AnswerState.WRONG -> stringResource(R.string.game_option_wrong)
                AnswerState.REVEALED_CORRECT -> stringResource(R.string.game_option_revealed)
                AnswerState.IDLE, AnswerState.DISABLED -> null
            },
        minHeight = if (tall) Lumora.sizes.huge else Lumora.sizes.xl,
        modifier =
            modifier.graphicsLayer {
                alpha = entrance.value
                translationY = (1f - entrance.value) * ENTER_OFFSET.toPx()
            },
    )
}

@Composable
private fun FeedbackRow(
    feedback: Feedback,
    onNext: () -> Unit,
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            text =
                when (feedback) {
                    Feedback.Correct -> stringResource(R.string.game_feedback_correct)
                    is Feedback.Wrong -> stringResource(R.string.game_feedback_wrong, feedback.correctAnswer)
                    is Feedback.TimedOut -> stringResource(R.string.game_feedback_timeout, feedback.correctAnswer)
                },
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.weight(1f).semantics { liveRegion = LiveRegionMode.Polite },
        )
        Box {
            TextButton(onClick = onNext) { Text(stringResource(R.string.game_next)) }
        }
    }
}

@Composable
private fun difficultyLabel(difficulty: Difficulty): String =
    when (difficulty) {
        Difficulty.EASY -> stringResource(R.string.game_difficulty_easy)
        Difficulty.MEDIUM -> stringResource(R.string.game_difficulty_medium)
        Difficulty.HARD -> stringResource(R.string.game_difficulty_hard)
        Difficulty.ANY -> ""
    }

private const val ENTER_SETTLE_MS = 300L
private val ENTER_OFFSET = 24.dp
