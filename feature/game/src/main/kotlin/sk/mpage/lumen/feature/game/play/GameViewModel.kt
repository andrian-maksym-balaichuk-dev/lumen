package sk.mpage.lumen.feature.game.play

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import sk.mpage.lumen.core.common.time.MonotonicClock
import sk.mpage.lumen.core.domain.game.FeedResult
import sk.mpage.lumen.core.domain.game.GameRules
import sk.mpage.lumen.core.domain.game.QuizAction
import sk.mpage.lumen.core.domain.game.QuizPhase
import sk.mpage.lumen.core.domain.game.QuizReducer
import sk.mpage.lumen.core.domain.game.QuizState
import sk.mpage.lumen.core.domain.repository.QuestionRepository
import sk.mpage.lumen.core.model.QuizConfig
import java.time.Clock
import java.time.ZonedDateTime
import java.util.UUID

@HiltViewModel(assistedFactory = GameViewModel.Factory::class)
internal class GameViewModel
    @AssistedInject
    constructor(
        @Assisted config: QuizConfig,
        questions: QuestionRepository,
        private val monotonicClock: MonotonicClock,
        private val clock: Clock,
    ) : ViewModel() {
        private val state = MutableStateFlow(QuizState(config = config, gameUuid = UUID.randomUUID().toString()))
        private val actions = Channel<QuizAction>(Channel.UNLIMITED)
        private val feed = questions.feed(config, viewModelScope)
        private var phaseJob: Job? = null

        val uiState: StateFlow<GameUiState> =
            state
                .map(QuizStateMapper::map)
                .stateIn(viewModelScope, SharingStarted.Eagerly, QuizStateMapper.map(state.value))

        init {
            viewModelScope.launch {
                enter(state.value)
                for (action in actions) {
                    val before = state.value
                    val after = QuizReducer.reduce(before, action)
                    if (after == before) continue
                    state.value = after
                    if (after.phase != before.phase) enter(after)
                }
            }
        }

        fun onAction(action: GameAction) {
            when (action) {
                is GameAction.OptionTapped -> {
                    dispatch(
                        QuizAction.AnswerSelected(
                            option = action.option,
                            presentationId = action.presentationId,
                            nowMs = monotonicClock.elapsedRealtimeMs(),
                            now = ZonedDateTime.now(clock),
                        ),
                    )
                }

                is GameAction.QuestionShown -> {
                    dispatch(QuizAction.QuestionReady(action.presentationId, monotonicClock.elapsedRealtimeMs()))
                }

                GameAction.NextTapped -> {
                    dispatch(QuizAction.Next)
                }

                GameAction.RetryTapped -> {
                    dispatch(QuizAction.Retry)
                }
            }
        }

        private fun dispatch(action: QuizAction) {
            actions.trySend(action)
        }

        private fun enter(state: QuizState) {
            phaseJob?.cancel()
            phaseJob =
                when (state.phase) {
                    QuizPhase.Loading -> {
                        viewModelScope.launch {
                            when (val result = feed.next()) {
                                is FeedResult.Next -> {
                                    dispatch(
                                        QuizAction.QuestionLoaded(
                                            result.question,
                                            state.attempts.size + 1,
                                        ),
                                    )
                                }

                                is FeedResult.Failed -> {
                                    dispatch(QuizAction.FeedFailed(result.error))
                                }
                            }
                        }
                    }

                    is QuizPhase.Reveal -> {
                        viewModelScope.launch {
                            delay(GameRules.REVEAL_DURATION_MS)
                            dispatch(QuizAction.Next)
                        }
                    }

                    else -> {
                        null
                    }
                }
        }

        override fun onCleared() {
            feed.close()
        }

        @AssistedFactory
        interface Factory {
            fun create(config: QuizConfig): GameViewModel
        }
    }
