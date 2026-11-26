package sk.mpage.lumen.core.testing.repository

import sk.mpage.lumen.core.common.result.AppResult
import sk.mpage.lumen.core.common.result.DataError
import sk.mpage.lumen.core.domain.repository.QuizHistoryRepository
import sk.mpage.lumen.core.domain.repository.SaveOutcome
import sk.mpage.lumen.core.model.FinishedGame

public class FakeQuizHistoryRepository : QuizHistoryRepository {
    private val failures = ArrayDeque<DataError>()
    private val games = mutableListOf<FinishedGame>()

    public val saved: List<FinishedGame> get() = games

    public fun failNext(error: DataError) {
        failures.addLast(error)
    }

    override suspend fun save(game: FinishedGame): AppResult<SaveOutcome> {
        failures.removeFirstOrNull()?.let { return AppResult.Failure(it) }
        val existing = games.indexOfFirst { it.gameUuid == game.gameUuid }
        if (existing >= 0) return AppResult.Success(SaveOutcome(existing + 1L, null, alreadySaved = true))
        games += game
        return AppResult.Success(SaveOutcome(games.size.toLong(), null, alreadySaved = false))
    }
}
