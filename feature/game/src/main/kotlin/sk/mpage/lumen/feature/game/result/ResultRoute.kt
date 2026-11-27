package sk.mpage.lumen.feature.game.result

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalResources
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import sk.mpage.lumen.core.model.QuizConfig
import sk.mpage.lumen.core.ui.ObserveAsEvents
import sk.mpage.lumen.core.ui.ShareTextFormatter
import sk.mpage.lumen.core.ui.shareText
import sk.mpage.lumen.feature.game.R

@Composable
internal fun ResultRoute(
    sessionId: Long,
    onClose: () -> Unit,
    onPlayAgain: (QuizConfig) -> Unit,
    onReview: (sessionId: Long) -> Unit,
    viewModel: ResultViewModel =
        hiltViewModel<ResultViewModel, ResultViewModel.Factory>(creationCallback = { it.create(sessionId) }),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val resources = LocalResources.current
    ObserveAsEvents(viewModel.effects) { effect ->
        when (effect) {
            is ResultEffect.ShareText -> {
                context.shareText(
                    text = ShareTextFormatter.format(effect.summary, resources),
                    chooserTitle = resources.getString(R.string.result_share),
                )
            }
        }
    }
    ResultScreen(
        state = state,
        onClose = onClose,
        onPlayAgain = onPlayAgain,
        onShare = viewModel::onShareTapped,
        onReview = { onReview(sessionId) },
    )
}
