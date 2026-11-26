package sk.mpage.lumen.feature.game.play

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
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
    GameScreen(state = state, onAction = viewModel::onAction, onClose = onClose)
}
