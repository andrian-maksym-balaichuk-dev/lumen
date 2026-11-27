package sk.mpage.lumen.feature.awards

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import sk.mpage.lumen.core.domain.repository.QuizHistoryRepository
import sk.mpage.lumen.core.domain.usecase.ObserveStreakUseCase
import javax.inject.Inject

@HiltViewModel
internal class AwardsViewModel
    @Inject
    constructor(
        observeStreak: ObserveStreakUseCase,
        history: QuizHistoryRepository,
    ) : ViewModel() {
        val uiState: StateFlow<AwardsUiState> =
            combine(observeStreak(), history.observeMarathonBest(), history.observeGameCount()) { streak, best, count ->
                AwardsUiState.Content(streak, best, count)
            }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), AwardsUiState.Loading)
    }
