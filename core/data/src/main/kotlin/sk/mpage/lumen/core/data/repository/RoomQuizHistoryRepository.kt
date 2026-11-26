package sk.mpage.lumen.core.data.repository

import android.database.SQLException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import sk.mpage.lumen.core.common.dispatcher.Dispatcher
import sk.mpage.lumen.core.common.dispatcher.LumenDispatcher
import sk.mpage.lumen.core.common.result.AppResult
import sk.mpage.lumen.core.common.result.DataError
import sk.mpage.lumen.core.data.mapper.toDetail
import sk.mpage.lumen.core.data.mapper.toEntity
import sk.mpage.lumen.core.data.mapper.toSessionEntity
import sk.mpage.lumen.core.database.source.QuizHistoryLocalDataSource
import sk.mpage.lumen.core.domain.repository.QuizHistoryRepository
import sk.mpage.lumen.core.domain.repository.SaveOutcome
import sk.mpage.lumen.core.model.FinishedGame
import sk.mpage.lumen.core.model.QuizSessionDetail
import javax.inject.Inject

internal class RoomQuizHistoryRepository
    @Inject
    constructor(
        private val local: QuizHistoryLocalDataSource,
        @Dispatcher(LumenDispatcher.IO) private val io: CoroutineDispatcher,
    ) : QuizHistoryRepository {
        override fun observeSessionDetail(sessionId: Long): Flow<QuizSessionDetail?> =
            local.observeSessionDetail(sessionId).map { it?.toDetail() }

        override suspend fun save(game: FinishedGame): AppResult<SaveOutcome> =
            withContext(io) {
                try {
                    val outcome = local.insertFinishedGame(game.toSessionEntity(), game.attempts.map { it.toEntity() })
                    AppResult.Success(SaveOutcome(outcome.sessionId, outcome.previousBest, outcome.alreadySaved))
                } catch (_: SQLException) {
                    AppResult.Failure(DataError.Storage)
                }
            }
    }
