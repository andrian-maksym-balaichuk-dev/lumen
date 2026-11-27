package sk.mpage.lumen.core.testing.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import sk.mpage.lumen.core.common.result.AppResult
import sk.mpage.lumen.core.common.result.DataError
import sk.mpage.lumen.core.domain.repository.QuizHistoryRepository
import sk.mpage.lumen.core.domain.repository.SaveOutcome
import sk.mpage.lumen.core.model.Category
import sk.mpage.lumen.core.model.FinishedGame
import sk.mpage.lumen.core.model.HistoryFilter
import sk.mpage.lumen.core.model.QuizSession
import sk.mpage.lumen.core.model.QuizSessionDetail
import java.time.LocalDate

public class FakeQuizHistoryRepository : QuizHistoryRepository {
    private val failures = ArrayDeque<DataError>()
    private val games = mutableListOf<FinishedGame>()

    public val saved: List<FinishedGame> get() = games

    public fun failNext(error: DataError) {
        failures.addLast(error)
    }

    private val sessions = MutableStateFlow<List<QuizSession>>(emptyList())
    private val filters = mutableListOf<HistoryFilter>()

    public val requestedFilters: List<HistoryFilter> get() = filters

    public fun emitSessions(value: List<QuizSession>) {
        sessions.value = value
    }

    override fun observeSessions(filter: HistoryFilter): Flow<List<QuizSession>> {
        filters += filter
        return sessions.map { list ->
            list.filter {
                (filter.mode == null || it.config.mode == filter.mode) &&
                    (filter.categoryId == null || it.config.categoryId == filter.categoryId)
            }
        }
    }

    private val played = MutableStateFlow<List<Category>>(emptyList())

    public fun emitPlayedCategories(value: List<Category>) {
        played.value = value
    }

    override fun observePlayedCategories(): Flow<List<Category>> = played

    private val details = MutableStateFlow<Map<Long, QuizSessionDetail>>(emptyMap())

    public fun emitDetail(detail: QuizSessionDetail) {
        details.update { it + (detail.session.id to detail) }
    }

    override fun observeSessionDetail(sessionId: Long): Flow<QuizSessionDetail?> = details.map { it[sessionId] }

    private val qualifyingDays = MutableStateFlow<List<LocalDate>>(emptyList())

    public fun emitQualifyingDays(value: List<LocalDate>) {
        qualifyingDays.value = value
    }

    override fun observeQualifyingDays(): Flow<List<LocalDate>> = qualifyingDays

    private val marathonBest = MutableStateFlow<Int?>(null)

    public fun emitMarathonBest(value: Int?) {
        marathonBest.value = value
    }

    override fun observeMarathonBest(): Flow<Int?> = marathonBest

    override fun observeGameCount(): Flow<Int> = sessions.map { it.size }

    override suspend fun save(game: FinishedGame): AppResult<SaveOutcome> {
        failures.removeFirstOrNull()?.let { return AppResult.Failure(it) }
        val existing = games.indexOfFirst { it.gameUuid == game.gameUuid }
        if (existing >= 0) return AppResult.Success(SaveOutcome(existing + 1L, null, alreadySaved = true))
        games += game
        return AppResult.Success(SaveOutcome(games.size.toLong(), null, alreadySaved = false))
    }
}
