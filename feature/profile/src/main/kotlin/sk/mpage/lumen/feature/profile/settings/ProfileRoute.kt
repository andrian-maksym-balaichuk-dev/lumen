package sk.mpage.lumen.feature.profile.settings

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalResources
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch
import sk.mpage.lumen.core.ui.ObserveAsEvents

@Composable
internal fun ProfileRoute(viewModel: ProfileViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val resources = LocalResources.current
    ObserveAsEvents(viewModel.effects) { effect ->
        when (effect) {
            is ProfileEffect.ShowMessage -> {
                scope.launch {
                    snackbarHostState.showSnackbar(
                        effect.message.asString(resources),
                    )
                }
            }
        }
    }
    ProfileScreen(state = state, onAction = viewModel::onAction, snackbarHostState = snackbarHostState)
}
