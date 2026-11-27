package sk.mpage.lumen.feature.history.detail

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
internal fun SessionDetailRoute(
    sessionId: Long,
    onBack: () -> Unit,
    viewModel: SessionDetailViewModel =
        hiltViewModel<SessionDetailViewModel, SessionDetailViewModel.Factory>(
            creationCallback = { it.create(sessionId) },
        ),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    SessionDetailScreen(state = state, onBack = onBack)
}
