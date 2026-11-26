package sk.mpage.lumen.core.domain.game

import sk.mpage.lumen.core.common.result.DataError
import sk.mpage.lumen.core.model.Question
import java.time.ZonedDateTime

public sealed interface QuizAction {
    public data class QuestionLoaded(
        val question: Question,
        val presentationId: Int,
    ) : QuizAction

    public data class QuestionReady(
        val presentationId: Int,
        val nowMs: Long,
    ) : QuizAction

    public data class AnswerSelected(
        val option: String,
        val presentationId: Int,
        val nowMs: Long,
        val now: ZonedDateTime,
    ) : QuizAction

    public data class TimeUp(
        val presentationId: Int,
        val nowMs: Long,
        val now: ZonedDateTime,
    ) : QuizAction

    public data object Next : QuizAction

    public data class FeedFailed(
        val error: DataError,
    ) : QuizAction

    public data class QuestionsExhausted(
        val nowMs: Long,
        val now: ZonedDateTime,
    ) : QuizAction

    public data object Retry : QuizAction

    public data object QuitConfirmed : QuizAction

    public data class Saved(
        val sessionId: Long,
        val isNewBest: Boolean,
    ) : QuizAction

    public data class SaveFailed(
        val error: DataError,
    ) : QuizAction
}
