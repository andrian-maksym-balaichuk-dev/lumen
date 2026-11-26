package sk.mpage.lumen.feature.game.play

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.delay
import sk.mpage.lumen.core.common.result.DataError
import sk.mpage.lumen.core.designsystem.component.AnswerState
import sk.mpage.lumen.core.designsystem.component.ErrorState
import sk.mpage.lumen.core.designsystem.icon.LumoraIcons
import sk.mpage.lumen.core.designsystem.preview.LumoraPreviews
import sk.mpage.lumen.core.designsystem.preview.PreviewSurface
import sk.mpage.lumen.core.designsystem.theme.Lumora
import sk.mpage.lumen.core.model.Difficulty
import sk.mpage.lumen.core.model.QuestionType
import sk.mpage.lumen.core.ui.toUiText
import sk.mpage.lumen.feature.game.R
import sk.mpage.lumen.feature.game.component.GameHeader
import sk.mpage.lumen.feature.game.component.QuestionContent

@Composable
internal fun GameScreen(
    state: GameUiState,
    remainingMs: () -> Long,
    onAction: (GameAction) -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier.fillMaxSize().safeDrawingPadding()) {
        val playing = state as? GameUiState.Playing
        GameHeader(
            position = playing?.position,
            total = playing?.total,
            remainingMs = remainingMs,
            onClose = onClose,
            modifier = Modifier.padding(horizontal = Lumora.spacing.sm),
        )
        Box(Modifier.weight(1f).fillMaxSize()) {
            when (state) {
                GameUiState.Loading -> {
                    Waiting(stringResource(R.string.game_loading))
                }

                GameUiState.Saving -> {
                    Waiting(stringResource(R.string.game_saving))
                }

                GameUiState.Waiting -> {
                    var showSpinner by remember { mutableStateOf(false) }
                    LaunchedEffect(Unit) {
                        delay(WAITING_SPINNER_DELAY_MS)
                        showSpinner = true
                    }
                    if (showSpinner) Waiting(stringResource(R.string.game_waiting))
                }

                is GameUiState.Stalled -> {
                    ErrorState(
                        title = stringResource(R.string.game_stalled),
                        message = state.error.toUiText().asString(),
                        retryLabel = stringResource(R.string.game_retry),
                        onRetry = { onAction(GameAction.RetryTapped) },
                        icon = if (state.error == DataError.NoInternet) LumoraIcons.CloudOff else LumoraIcons.Error,
                        secondaryLabel = stringResource(R.string.game_quit_discard),
                        onSecondary = { onAction(GameAction.QuitConfirmed) },
                        modifier = Modifier.align(Alignment.Center).padding(Lumora.spacing.gutterCompact),
                    )
                }

                is GameUiState.SaveFailed -> {
                    ErrorState(
                        title = stringResource(R.string.game_save_failed),
                        message = state.error.toUiText().asString(),
                        retryLabel = stringResource(R.string.game_retry),
                        onRetry = { onAction(GameAction.RetryTapped) },
                        modifier = Modifier.align(Alignment.Center).padding(Lumora.spacing.gutterCompact),
                    )
                }

                is GameUiState.LoadFailed -> {
                    ErrorState(
                        title = stringResource(R.string.game_load_failed),
                        message = state.error.toUiText().asString(),
                        retryLabel = stringResource(R.string.game_retry),
                        onRetry = { onAction(GameAction.RetryTapped) },
                        icon = if (state.error == DataError.NoInternet) LumoraIcons.CloudOff else LumoraIcons.Error,
                        modifier = Modifier.align(Alignment.Center).padding(Lumora.spacing.gutterCompact),
                    )
                }

                is GameUiState.Playing -> {
                    QuestionContent(
                        state = state,
                        onOptionTapped = { onAction(GameAction.OptionTapped(it, state.presentationId)) },
                        onShown = { onAction(GameAction.QuestionShown(state.presentationId)) },
                        onNext = { onAction(GameAction.NextTapped) },
                    )
                }
            }
        }
    }
}

@Composable
private fun Waiting(label: String) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(Lumora.spacing.lg, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        CircularProgressIndicator()
        Text(label, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onBackground)
    }
}

@LumoraPreviews
@Composable
private fun GameQuestionPreview() {
    PreviewSurface {
        GameScreen(state = PreviewPlaying, remainingMs = { 12_000 }, onAction = {}, onClose = {})
    }
}

@LumoraPreviews
@Composable
private fun GameRevealPreview() {
    PreviewSurface {
        GameScreen(
            state =
                PreviewPlaying.copy(
                    options =
                        persistentListOf(
                            OptionUi("Mars", AnswerState.DISABLED),
                            OptionUi("Jupiter", AnswerState.REVEALED_CORRECT),
                            OptionUi("Earth", AnswerState.WRONG),
                            OptionUi("Venus", AnswerState.DISABLED),
                        ),
                    acceptsAnswers = false,
                    feedback = Feedback.Wrong("Jupiter"),
                ),
            remainingMs = { 7_000 },
            onAction = {},
            onClose = {},
        )
    }
}

@LumoraPreviews
@Composable
private fun GameLoadFailedPreview() {
    PreviewSurface {
        GameScreen(
            state = GameUiState.LoadFailed(DataError.NoInternet),
            remainingMs = { 0 },
            onAction = {},
            onClose = {},
        )
    }
}

@LumoraPreviews
@Composable
private fun GameStalledPreview() {
    PreviewSurface {
        GameScreen(state = GameUiState.Stalled(DataError.NoInternet), remainingMs = { 0 }, onAction = {}, onClose = {})
    }
}

private const val WAITING_SPINNER_DELAY_MS = 400L

private val PreviewPlaying =
    GameUiState.Playing(
        position = 6,
        total = 10,
        categoryName = "Science & Nature",
        difficulty = Difficulty.MEDIUM,
        type = QuestionType.MULTIPLE_CHOICE,
        text = "Which planet has the shortest day in the Solar System?",
        presentationId = 6,
        options =
            persistentListOf(
                OptionUi("Mars", AnswerState.IDLE),
                OptionUi("Jupiter", AnswerState.IDLE),
                OptionUi("Earth", AnswerState.IDLE),
                OptionUi("Venus", AnswerState.IDLE),
            ),
        acceptsAnswers = true,
        feedback = null,
    )
