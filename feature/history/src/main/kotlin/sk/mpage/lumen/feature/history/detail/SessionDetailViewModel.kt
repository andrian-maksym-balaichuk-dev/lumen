package sk.mpage.lumen.feature.history.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import sk.mpage.lumen.core.domain.repository.QuizHistoryRepository

@HiltViewModel(assistedFactory = SessionDetailViewModel.Factory::class)
internal class SessionDetailViewModel
    @AssistedInject
    constructor(
        @Assisted sessionId: Long,
        history: QuizHistoryRepository,
    ) : ViewModel() {
        val uiState: StateFlow<SessionDetailUiState> =
            history
                .observeSessionDetail(sessionId)
                .map { detail ->
                    if (detail == null) {
                        SessionDetailUiState.NotFound
                    } else {
                        SessionDetailUiState.Content(
                            detail.session,
                            detail.attempts.sortedBy { it.position }.toImmutableList(),
                        )
                    }
                }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SessionDetailUiState.Loading)

        @AssistedFactory
        interface Factory {
            fun create(sessionId: Long): SessionDetailViewModel
        }
    }
