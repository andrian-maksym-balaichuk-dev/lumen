package sk.mpage.lumen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import sk.mpage.lumen.core.domain.repository.UserProfileRepository
import javax.inject.Inject

internal sealed interface StartupUiState {
    data object Loading : StartupUiState

    data object NeedsOnboarding : StartupUiState

    data object Ready : StartupUiState
}

@HiltViewModel
internal class MainActivityViewModel
    @Inject
    constructor(
        profiles: UserProfileRepository,
    ) : ViewModel() {
        val uiState: StateFlow<StartupUiState> =
            flow {
                val onboarded = profiles.observeProfile().first().onboarded
                emit(if (onboarded) StartupUiState.Ready else StartupUiState.NeedsOnboarding)
            }.stateIn(viewModelScope, SharingStarted.Eagerly, StartupUiState.Loading)
    }
