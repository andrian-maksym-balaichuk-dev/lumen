package sk.mpage.lumen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import sk.mpage.lumen.core.domain.repository.UserProfileRepository
import sk.mpage.lumen.core.domain.usecase.ObserveStreakUseCase
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
        observeStreak: ObserveStreakUseCase,
    ) : ViewModel() {
        val uiState: StateFlow<StartupUiState> =
            flow {
                val onboarded = profiles.observeProfile().first().onboarded
                emit(if (onboarded) StartupUiState.Ready else StartupUiState.NeedsOnboarding)
            }.stateIn(viewModelScope, SharingStarted.Eagerly, StartupUiState.Loading)

        private val openSession = MutableStateFlow<Long?>(null)
        val sessionToOpen: StateFlow<Long?> = openSession.asStateFlow()

        fun onSessionLink(sessionId: Long) {
            openSession.value = sessionId
        }

        fun onSessionOpened() {
            openSession.value = null
        }

        val streakAtRisk: StateFlow<Boolean> =
            observeStreak()
                .map { it.atRisk }
                .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)
    }
