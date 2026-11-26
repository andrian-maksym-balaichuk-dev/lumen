package sk.mpage.lumen.feature.game.play

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.LifecycleStartEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import sk.mpage.lumen.core.model.QuizConfig
import sk.mpage.lumen.core.ui.ObserveAsEvents
import sk.mpage.lumen.feature.game.component.QuitDialog

@Composable
internal fun GameRoute(
    config: QuizConfig,
    onClose: () -> Unit,
    onShowResults: (sessionId: Long) -> Unit,
    viewModel: GameViewModel =
        hiltViewModel<GameViewModel, GameViewModel.Factory>(creationCallback = { factory -> factory.create(config) }),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val remainingMs = viewModel.remainingMs.collectAsStateWithLifecycle()
    ObserveAsEvents(viewModel.effects) { effect ->
        when (effect) {
            is GameEffect.ShowResults -> onShowResults(effect.sessionId)
            GameEffect.Closed -> onClose()
        }
    }
    LifecycleStartEffect(viewModel) {
        viewModel.onAction(GameAction.VisibilityChanged(true))
        onStopOrDispose { viewModel.onAction(GameAction.VisibilityChanged(false)) }
    }
    var confirmQuit by rememberSaveable { mutableStateOf(false) }
    BackHandler { confirmQuit = true }
    GameScreen(
        state = state,
        remainingMs = { remainingMs.value },
        onAction = viewModel::onAction,
        onClose = { confirmQuit = true },
    )
    if (confirmQuit) {
        QuitDialog(
            onConfirm = {
                confirmQuit = false
                viewModel.onAction(GameAction.QuitConfirmed)
            },
            onDismiss = { confirmQuit = false },
        )
    }
}
