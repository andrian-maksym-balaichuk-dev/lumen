package sk.mpage.lumen.feature.game.play

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.LifecycleStartEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import sk.mpage.lumen.core.model.QuizConfig

@Composable
internal fun GameRoute(
    config: QuizConfig,
    onClose: () -> Unit,
    viewModel: GameViewModel =
        hiltViewModel<GameViewModel, GameViewModel.Factory>(creationCallback = { factory -> factory.create(config) }),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val remainingMs = viewModel.remainingMs.collectAsStateWithLifecycle()
    LifecycleStartEffect(viewModel) {
        viewModel.onAction(GameAction.VisibilityChanged(true))
        onStopOrDispose { viewModel.onAction(GameAction.VisibilityChanged(false)) }
    }
    GameScreen(
        state = state,
        remainingMs = { remainingMs.value },
        onAction = viewModel::onAction,
        onClose = onClose,
    )
}
