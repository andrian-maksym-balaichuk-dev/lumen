package sk.mpage.lumen.core.database.source

import kotlinx.coroutines.flow.Flow
import sk.mpage.lumen.core.database.dao.InsertOutcome
import sk.mpage.lumen.core.database.dao.PlayedCategory
import sk.mpage.lumen.core.database.dao.QuizSessionDao
import sk.mpage.lumen.core.database.entity.QuestionAttemptEntity
import sk.mpage.lumen.core.database.entity.QuizSessionEntity
import sk.mpage.lumen.core.database.relation.SessionWithAttempts
import sk.mpage.lumen.core.model.GameMode
import sk.mpage.lumen.core.model.HistoryFilter
import sk.mpage.lumen.core.model.HistorySort
import java.time.Instant
import java.time.LocalDate
import javax.inject.Inject

public class QuizHistoryLocalDataSource
    @Inject
    internal constructor(
        private val dao: QuizSessionDao,
    ) {
        public fun observeSessions(filter: HistoryFilter): Flow<List<QuizSessionEntity>> =
            when (filter.sort) {
                HistorySort.NEWEST -> dao.observeNewest(filter.categoryId, filter.mode)
                HistorySort.BEST_SCORE -> dao.observeBestScore(filter.categoryId, filter.mode)
                HistorySort.FASTEST -> dao.observeFastest(filter.categoryId, filter.mode)
            }

        public suspend fun insertFinishedGame(
            session: QuizSessionEntity,
            attempts: List<QuestionAttemptEntity>,
        ): InsertOutcome = dao.insertFinishedGame(session, attempts)

        public fun observeSessionDetail(id: Long): Flow<SessionWithAttempts?> = dao.observeWithAttempts(id)

        public fun observeQualifyingDays(minPercent: Int): Flow<List<LocalDate>> = dao.observeQualifyingDays(minPercent)

        public fun observeMarathonBest(): Flow<Int?> = dao.observeMaxCorrect(GameMode.MARATHON)

        public suspend fun marathonBestBefore(sessionId: Long): Int? =
            dao.maxCorrectBefore(GameMode.MARATHON, sessionId)

        public fun observeCount(): Flow<Int> = dao.observeCount()

        public fun observePlayedCategories(): Flow<List<PlayedCategory>> = dao.observePlayedCategories()

        public suspend fun lastFinishedAt(): Instant? = dao.lastFinishedAt()

        public suspend fun delete(id: Long): Unit = dao.delete(id)
    }
