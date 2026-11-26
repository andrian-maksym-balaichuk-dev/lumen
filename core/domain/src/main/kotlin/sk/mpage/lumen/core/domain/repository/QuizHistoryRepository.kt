package sk.mpage.lumen.core.domain.repository

import sk.mpage.lumen.core.common.result.AppResult
import sk.mpage.lumen.core.model.FinishedGame

public interface QuizHistoryRepository {
    public suspend fun save(game: FinishedGame): AppResult<SaveOutcome>
}

public data class SaveOutcome(
    val sessionId: Long,
    val previousMarathonBest: Int?,
    val alreadySaved: Boolean,
)
