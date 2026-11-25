package sk.mpage.lumen.feature.lobby

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
internal fun LobbyRoute(
    onOpenProfile: () -> Unit,
    viewModel: LobbyViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    LobbyScreen(state = state, onAction = viewModel::onAction, onOpenProfile = onOpenProfile)
}
