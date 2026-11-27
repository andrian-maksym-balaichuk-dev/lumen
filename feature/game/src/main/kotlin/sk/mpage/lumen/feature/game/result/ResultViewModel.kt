package sk.mpage.lumen.feature.game.result

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import sk.mpage.lumen.core.domain.game.GameRules
import sk.mpage.lumen.core.domain.repository.QuizHistoryRepository
import sk.mpage.lumen.core.domain.repository.UserProfileRepository
import sk.mpage.lumen.core.domain.usecase.BuildShareSummaryUseCase
import sk.mpage.lumen.core.model.EndReason
import sk.mpage.lumen.core.model.GameMode

@HiltViewModel(assistedFactory = ResultViewModel.Factory::class)
internal class ResultViewModel
    @AssistedInject
    constructor(
        @Assisted sessionId: Long,
        history: QuizHistoryRepository,
        profiles: UserProfileRepository,
        private val buildShareSummary: BuildShareSummaryUseCase,
    ) : ViewModel() {
        private val session = history.observeSessionDetail(sessionId).map { it?.session }

        private val previousBest = flow { emit(history.marathonBestBefore(sessionId)) }

        private val effectChannel = Channel<ResultEffect>(Channel.BUFFERED)
        val effects: Flow<ResultEffect> = effectChannel.receiveAsFlow()

        val uiState: StateFlow<ResultUiState> =
            combine(session, profiles.observeProfile(), previousBest) { session, profile, previousBest ->
                if (session == null) return@combine ResultUiState.NotFound
                ResultUiState.Content(
                    playerName = profile.name,
                    avatar = profile.avatar,
                    config = session.config,
                    answeredEveryQuestion =
                        session.config.mode == GameMode.MARATHON && session.endReason == EndReason.COMPLETED,
                    correct = session.correctCount,
                    total = session.totalQuestions,
                    wrong = session.wrongCount,
                    timeouts = session.timeoutCount,
                    percent = session.scorePercent,
                    durationSeconds = session.durationSeconds,
                    averageAnswerMs = session.averageAnswerMs,
                    isNewBest = GameRules.isNewMarathonBest(session.config.mode, session.correctCount, previousBest),
                    previousBest = previousBest,
                )
            }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ResultUiState.Loading)

        fun onShareTapped() {
            viewModelScope.launch {
                val current = session.first() ?: return@launch
                val isNewBest = (uiState.value as? ResultUiState.Content)?.isNewBest == true
                effectChannel.send(ResultEffect.ShareText(buildShareSummary(current, isNewBest)))
            }
        }

        @AssistedFactory
        interface Factory {
            fun create(sessionId: Long): ResultViewModel
        }
    }
