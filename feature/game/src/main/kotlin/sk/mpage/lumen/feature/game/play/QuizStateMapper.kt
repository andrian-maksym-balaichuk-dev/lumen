package sk.mpage.lumen.feature.game.play

import kotlinx.collections.immutable.toImmutableList
import sk.mpage.lumen.core.designsystem.component.AnswerState
import sk.mpage.lumen.core.domain.game.GameRules
import sk.mpage.lumen.core.domain.game.QuizPhase
import sk.mpage.lumen.core.domain.game.QuizState
import sk.mpage.lumen.core.model.GameMode
import sk.mpage.lumen.core.model.Question
import sk.mpage.lumen.core.model.QuestionAttempt

internal object QuizStateMapper {
    fun map(state: QuizState): GameUiState =
        when (val phase = state.phase) {
            QuizPhase.Loading -> GameUiState.Loading

            is QuizPhase.LoadFailed -> GameUiState.LoadFailed(phase.error)

            is QuizPhase.Presenting -> playing(state, phase.question, phase.presentationId, attempt = null)

            is QuizPhase.Answering -> playing(state, phase.question, phase.presentationId, attempt = null)

            is QuizPhase.Reveal -> playing(state, phase.attempt.question, phase.attempt.position, phase.attempt)

            is QuizPhase.SaveFailed -> GameUiState.SaveFailed(phase.error)

            is QuizPhase.Saving,
            is QuizPhase.Finished,
            QuizPhase.Abandoned,
            -> GameUiState.Saving
        }

    private fun playing(
        state: QuizState,
        question: Question,
        presentationId: Int,
        attempt: QuestionAttempt?,
    ): GameUiState.Playing =
        GameUiState.Playing(
            position = attempt?.position ?: (state.attempts.size + 1),
            total = if (state.config.mode == GameMode.CLASSIC) GameRules.CLASSIC_QUESTION_COUNT else null,
            categoryName = question.categoryName,
            difficulty = question.difficulty,
            type = question.type,
            text = question.text,
            presentationId = presentationId,
            options = question.options.map { OptionUi(it, optionState(it, question, attempt)) }.toImmutableList(),
            feedback = attempt?.let(::feedback),
        )

    private fun optionState(
        option: String,
        question: Question,
        attempt: QuestionAttempt?,
    ): AnswerState =
        when {
            attempt == null -> AnswerState.IDLE
            option == attempt.selectedAnswer && attempt.isCorrect -> AnswerState.CORRECT
            option == attempt.selectedAnswer -> AnswerState.WRONG
            option == question.correctAnswer -> AnswerState.REVEALED_CORRECT
            else -> AnswerState.DISABLED
        }

    private fun feedback(attempt: QuestionAttempt): Feedback =
        when {
            attempt.isCorrect -> Feedback.Correct
            attempt.selectedAnswer == null -> Feedback.TimedOut(attempt.question.correctAnswer)
            else -> Feedback.Wrong(attempt.question.correctAnswer)
        }
}
