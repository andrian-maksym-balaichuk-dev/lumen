package sk.mpage.lumen.core.domain.repository

import kotlinx.coroutines.flow.Flow
import sk.mpage.lumen.core.common.result.AppResult
import sk.mpage.lumen.core.model.FinishedGame
import sk.mpage.lumen.core.model.QuizSessionDetail

public interface QuizHistoryRepository {
    public fun observeSessionDetail(sessionId: Long): Flow<QuizSessionDetail?>

    public suspend fun save(game: FinishedGame): AppResult<SaveOutcome>
}

public data class SaveOutcome(
    val sessionId: Long,
    val previousMarathonBest: Int?,
    val alreadySaved: Boolean,
)
