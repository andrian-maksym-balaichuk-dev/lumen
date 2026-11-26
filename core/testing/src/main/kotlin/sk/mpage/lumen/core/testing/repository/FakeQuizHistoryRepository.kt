package sk.mpage.lumen.core.testing.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import sk.mpage.lumen.core.common.result.AppResult
import sk.mpage.lumen.core.common.result.DataError
import sk.mpage.lumen.core.domain.repository.QuizHistoryRepository
import sk.mpage.lumen.core.domain.repository.SaveOutcome
import sk.mpage.lumen.core.model.FinishedGame
import sk.mpage.lumen.core.model.QuizSessionDetail

public class FakeQuizHistoryRepository : QuizHistoryRepository {
    private val failures = ArrayDeque<DataError>()
    private val games = mutableListOf<FinishedGame>()

    public val saved: List<FinishedGame> get() = games

    public fun failNext(error: DataError) {
        failures.addLast(error)
    }

    private val details = MutableStateFlow<Map<Long, QuizSessionDetail>>(emptyMap())

    public fun emitDetail(detail: QuizSessionDetail) {
        details.update { it + (detail.session.id to detail) }
    }

    override fun observeSessionDetail(sessionId: Long): Flow<QuizSessionDetail?> = details.map { it[sessionId] }

    override suspend fun save(game: FinishedGame): AppResult<SaveOutcome> {
        failures.removeFirstOrNull()?.let { return AppResult.Failure(it) }
        val existing = games.indexOfFirst { it.gameUuid == game.gameUuid }
        if (existing >= 0) return AppResult.Success(SaveOutcome(existing + 1L, null, alreadySaved = true))
        games += game
        return AppResult.Success(SaveOutcome(games.size.toLong(), null, alreadySaved = false))
    }
}
