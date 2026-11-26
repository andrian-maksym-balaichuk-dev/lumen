package sk.mpage.lumen.core.domain.game

import sk.mpage.lumen.core.domain.game.QuizPhase.Abandoned
import sk.mpage.lumen.core.domain.game.QuizPhase.Answering
import sk.mpage.lumen.core.domain.game.QuizPhase.Finished
import sk.mpage.lumen.core.domain.game.QuizPhase.LoadFailed
import sk.mpage.lumen.core.domain.game.QuizPhase.Loading
import sk.mpage.lumen.core.domain.game.QuizPhase.Presenting
import sk.mpage.lumen.core.domain.game.QuizPhase.Reveal
import sk.mpage.lumen.core.domain.game.QuizPhase.Saving
import sk.mpage.lumen.core.model.EndReason
import sk.mpage.lumen.core.model.FinishedGame
import sk.mpage.lumen.core.model.GameMode
import sk.mpage.lumen.core.model.QuestionAttempt
import java.time.ZonedDateTime

public object QuizReducer {
    public fun reduce(
        state: QuizState,
        action: QuizAction,
    ): QuizState {
        val phase = state.phase
        return when (action) {
            is QuizAction.QuestionLoaded -> {
                if (phase !is Loading) return state
                state.copy(phase = Presenting(action.question, action.presentationId))
            }

            is QuizAction.QuestionReady -> {
                if (phase !is Presenting || phase.presentationId != action.presentationId) return state
                state.copy(
                    firstShownAtMs = state.firstShownAtMs ?: action.nowMs,
                    phase =
                        Answering(
                            question = phase.question,
                            presentationId = phase.presentationId,
                            shownAtMs = action.nowMs,
                            deadlineMs = action.nowMs + GameRules.QUESTION_TIME_LIMIT_MS,
                        ),
                )
            }

            is QuizAction.AnswerSelected -> {
                if (phase !is Answering || phase.presentationId != action.presentationId) return state
                if (action.option !in phase.question.options) return state
                if (action.nowMs >= phase.deadlineMs) {
                    record(state, phase, selected = null, nowMs = action.nowMs, now = action.now)
                } else {
                    record(state, phase, selected = action.option, nowMs = action.nowMs, now = action.now)
                }
            }

            is QuizAction.TimeUp -> {
                if (phase !is Answering || phase.presentationId != action.presentationId) return state
                if (action.nowMs < phase.deadlineMs) return state
                record(state, phase, selected = null, nowMs = action.nowMs, now = action.now)
            }

            QuizAction.Next -> {
                if (phase !is Reveal) return state
                state.copy(phase = phase.finished?.let(::Saving) ?: Loading)
            }

            is QuizAction.FeedFailed -> {
                if (phase !is Loading) return state
                state.copy(phase = LoadFailed(action.error))
            }

            QuizAction.Retry -> {
                when (phase) {
                    is LoadFailed -> state.copy(phase = Loading)
                    is QuizPhase.SaveFailed -> state.copy(phase = Saving(phase.game))
                    else -> state
                }
            }

            QuizAction.QuitConfirmed -> {
                val canQuit =
                    phase is Loading || phase is LoadFailed || phase is Presenting || phase is Answering ||
                        (phase is Reveal && phase.finished == null)
                if (canQuit) state.copy(phase = Abandoned) else state
            }

            is QuizAction.Saved -> {
                if (phase !is Saving) return state
                state.copy(phase = Finished(phase.game, action.sessionId, action.isNewBest))
            }

            is QuizAction.SaveFailed -> {
                if (phase !is Saving) return state
                state.copy(phase = QuizPhase.SaveFailed(phase.game, action.error))
            }
        }
    }

    private fun record(
        state: QuizState,
        phase: Answering,
        selected: String?,
        nowMs: Long,
        now: ZonedDateTime,
    ): QuizState {
        val timeSpentMs =
            if (selected == null) {
                GameRules.QUESTION_TIME_LIMIT_MS
            } else {
                (nowMs - phase.shownAtMs).coerceIn(0, GameRules.QUESTION_TIME_LIMIT_MS)
            }
        val attempt =
            QuestionAttempt(
                position = state.attempts.size + 1,
                question = phase.question,
                selectedAnswer = selected,
                isCorrect = selected == phase.question.correctAnswer,
                timeSpentMs = timeSpentMs,
                answeredAt = now.toInstant(),
            )
        val attempts = state.attempts + attempt
        val isLast = state.config.mode == GameMode.CLASSIC && attempts.size == GameRules.CLASSIC_QUESTION_COUNT
        val finished = if (isLast) finish(state, attempts, nowMs, now) else null
        return state.copy(attempts = attempts, phase = Reveal(attempt, finished))
    }

    private fun finish(
        state: QuizState,
        attempts: List<QuestionAttempt>,
        nowMs: Long,
        now: ZonedDateTime,
    ): FinishedGame {
        val elapsedMs = nowMs - (state.firstShownAtMs ?: nowMs)
        val finishedAt = now.toInstant()
        return FinishedGame(
            gameUuid = state.gameUuid,
            config = state.config,
            attempts = attempts,
            startedAt = finishedAt.minusMillis(elapsedMs),
            finishedAt = finishedAt,
            localDate = now.toLocalDate(),
            durationSeconds = elapsedMs / 1_000,
            endReason = EndReason.COMPLETED,
        )
    }
}
