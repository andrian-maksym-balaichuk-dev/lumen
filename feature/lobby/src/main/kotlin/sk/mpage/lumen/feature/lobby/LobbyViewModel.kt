package sk.mpage.lumen.feature.lobby

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import sk.mpage.lumen.core.domain.repository.UserProfileRepository
import java.time.Clock
import java.time.LocalTime
import javax.inject.Inject

@HiltViewModel
internal class LobbyViewModel
    @Inject
    constructor(
        profiles: UserProfileRepository,
        clock: Clock,
    ) : ViewModel() {
        private val selection = MutableStateFlow(LobbyUiState(greeting = Greeting.at(LocalTime.now(clock))))

        val uiState: StateFlow<LobbyUiState> =
            combine(selection, profiles.observeProfile()) { state, profile ->
                state.copy(playerName = profile.name, avatar = profile.avatar)
            }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), selection.value)

        fun onAction(action: LobbyAction) {
            when (action) {
                is LobbyAction.ModeSelected -> selection.update { it.copy(mode = action.mode) }
                is LobbyAction.DifficultySelected -> selection.update { it.copy(difficulty = action.difficulty) }
                is LobbyAction.TypeSelected -> selection.update { it.copy(type = action.type) }
            }
        }
    }
