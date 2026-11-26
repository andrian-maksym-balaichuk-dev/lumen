package sk.mpage.lumen.core.domain.game

import kotlinx.serialization.Serializable
import sk.mpage.lumen.core.common.result.DataError
import sk.mpage.lumen.core.model.FinishedGame
import sk.mpage.lumen.core.model.Question
import sk.mpage.lumen.core.model.QuestionAttempt
import sk.mpage.lumen.core.model.QuizConfig

@Serializable
public data class QuizState(
    val config: QuizConfig,
    val gameUuid: String,
    val attempts: List<QuestionAttempt> = emptyList(),
    val firstShownAtMs: Long? = null,
    val phase: QuizPhase = QuizPhase.Loading,
)

@Serializable
public sealed interface QuizPhase {
    @Serializable
    public data object Loading : QuizPhase

    @Serializable
    public data class LoadFailed(
        val error: DataError,
    ) : QuizPhase

    @Serializable
    public data object Waiting : QuizPhase

    @Serializable
    public data class Stalled(
        val error: DataError,
    ) : QuizPhase

    @Serializable
    public data class Presenting(
        val question: Question,
        val presentationId: Int,
    ) : QuizPhase

    @Serializable
    public data class Answering(
        val question: Question,
        val presentationId: Int,
        val shownAtMs: Long,
        val deadlineMs: Long,
    ) : QuizPhase

    @Serializable
    public data class Reveal(
        val attempt: QuestionAttempt,
        val finished: FinishedGame?,
    ) : QuizPhase

    @Serializable
    public data class Saving(
        val game: FinishedGame,
    ) : QuizPhase

    @Serializable
    public data class SaveFailed(
        val game: FinishedGame,
        val error: DataError,
    ) : QuizPhase

    @Serializable
    public data class Finished(
        val game: FinishedGame,
        val sessionId: Long,
        val isNewBest: Boolean,
    ) : QuizPhase

    @Serializable
    public data object Abandoned : QuizPhase
}
