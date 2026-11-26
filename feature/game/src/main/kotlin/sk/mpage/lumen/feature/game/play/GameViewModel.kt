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
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import sk.mpage.lumen.core.common.result.AppResult
import sk.mpage.lumen.core.common.time.MonotonicClock
import sk.mpage.lumen.core.domain.game.FeedResult
import sk.mpage.lumen.core.domain.game.GameRules
import sk.mpage.lumen.core.domain.game.QuizAction
import sk.mpage.lumen.core.domain.game.QuizPhase
import sk.mpage.lumen.core.domain.game.QuizReducer
import sk.mpage.lumen.core.domain.game.QuizState
import sk.mpage.lumen.core.domain.repository.QuestionRepository
import sk.mpage.lumen.core.domain.usecase.SaveQuizResultUseCase
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
        private val saveResult: SaveQuizResultUseCase,
        private val monotonicClock: MonotonicClock,
        private val clock: Clock,
    ) : ViewModel() {
        private val state = MutableStateFlow(QuizState(config = config, gameUuid = UUID.randomUUID().toString()))
        private val actions = Channel<QuizAction>(Channel.UNLIMITED)
        private val feed = questions.feed(config, viewModelScope)
        private var phaseJob: Job? = null
        private val visible = MutableStateFlow(true)
        private val remaining = MutableStateFlow(GameRules.QUESTION_TIME_LIMIT_MS)
        private var shownWhileHidden: Int? = null

        val remainingMs: StateFlow<Long> = remaining

        private val effectChannel = Channel<GameEffect>(Channel.BUFFERED)
        val effects: Flow<GameEffect> = effectChannel.receiveAsFlow()

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
                    if (visible.value) {
                        dispatch(QuizAction.QuestionReady(action.presentationId, monotonicClock.elapsedRealtimeMs()))
                    } else {
                        shownWhileHidden = action.presentationId
                    }
                }

                is GameAction.VisibilityChanged -> {
                    visible.value = action.visible
                    val pending = shownWhileHidden
                    if (action.visible && pending != null) {
                        shownWhileHidden = null
                        dispatch(QuizAction.QuestionReady(pending, monotonicClock.elapsedRealtimeMs()))
                    }
                }

                GameAction.NextTapped -> {
                    dispatch(QuizAction.Next)
                }

                GameAction.RetryTapped -> {
                    dispatch(QuizAction.Retry)
                }

                GameAction.QuitConfirmed -> {
                    dispatch(QuizAction.QuitConfirmed)
                }
            }
        }

        private fun dispatch(action: QuizAction) {
            actions.trySend(action)
        }

        private fun enter(state: QuizState) {
            phaseJob?.cancel()
            phaseJob =
                when (val phase = state.phase) {
                    QuizPhase.Loading, QuizPhase.Waiting -> {
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

                                FeedResult.Exhausted -> {
                                    dispatch(
                                        QuizAction.QuestionsExhausted(
                                            monotonicClock.elapsedRealtimeMs(),
                                            ZonedDateTime.now(clock),
                                        ),
                                    )
                                }
                            }
                        }
                    }

                    is QuizPhase.Presenting -> {
                        remaining.value = GameRules.QUESTION_TIME_LIMIT_MS
                        null
                    }

                    is QuizPhase.Answering -> {
                        viewModelScope.launch { runTimer(phase) }
                    }

                    is QuizPhase.Reveal -> {
                        if (phase.attempt.selectedAnswer == null) remaining.value = 0
                        viewModelScope.launch {
                            visible.first { it }
                            delay(GameRules.REVEAL_DURATION_MS)
                            dispatch(QuizAction.Next)
                        }
                    }

                    is QuizPhase.Saving -> {
                        feed.close()
                        viewModelScope.launch {
                            when (val result = saveResult(phase.game)) {
                                is AppResult.Success -> {
                                    dispatch(QuizAction.Saved(result.value.sessionId, result.value.isNewBest))
                                }

                                is AppResult.Failure -> {
                                    dispatch(QuizAction.SaveFailed(result.error))
                                }
                            }
                        }
                    }

                    QuizPhase.Abandoned -> {
                        feed.close()
                        viewModelScope.launch { effectChannel.send(GameEffect.Closed) }
                    }

                    is QuizPhase.Finished -> {
                        viewModelScope.launch { effectChannel.send(GameEffect.ShowResults(phase.sessionId)) }
                    }

                    else -> {
                        null
                    }
                }
        }

        private suspend fun runTimer(phase: QuizPhase.Answering) {
            while (true) {
                val now = monotonicClock.elapsedRealtimeMs()
                val left = (phase.deadlineMs - now).coerceAtLeast(0)
                remaining.value = left
                if (left == 0L) {
                    dispatch(QuizAction.TimeUp(phase.presentationId, now, ZonedDateTime.now(clock)))
                    return
                }
                delay(minOf(TICK_MS, left))
            }
        }

        override fun onCleared() {
            feed.close()
        }

        private companion object {
            const val TICK_MS = 100L
        }

        @AssistedFactory
        interface Factory {
            fun create(config: QuizConfig): GameViewModel
        }
    }
