package sk.mpage.lumen.core.domain.usecase

import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.test.runTest
import sk.mpage.lumen.core.common.result.AppResult
import sk.mpage.lumen.core.common.result.DataError
import sk.mpage.lumen.core.domain.repository.QuizHistoryRepository
import sk.mpage.lumen.core.domain.repository.SaveOutcome
import sk.mpage.lumen.core.model.Category
import sk.mpage.lumen.core.model.Difficulty
import sk.mpage.lumen.core.model.EndReason
import sk.mpage.lumen.core.model.FinishedGame
import sk.mpage.lumen.core.model.GameMode
import sk.mpage.lumen.core.model.HistoryFilter
import sk.mpage.lumen.core.model.QuestionType
import sk.mpage.lumen.core.model.QuizConfig
import sk.mpage.lumen.core.model.QuizSession
import sk.mpage.lumen.core.model.QuizSessionDetail
import java.time.Instant
import java.time.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals

class SaveQuizResultUseCaseTest {
    @Test
    fun `maps the outcome to the session id`() =
        runTest {
            val useCase = SaveQuizResultUseCase(repository(AppResult.Success(SaveOutcome(42, null, false))))

            assertEquals(AppResult.Success(SaveResult(42, isNewBest = false, previousBest = null)), useCase(GAME))
        }

    @Test
    fun `passes a storage failure through`() =
        runTest {
            val useCase = SaveQuizResultUseCase(repository(AppResult.Failure(DataError.Storage)))

            assertEquals(AppResult.Failure(DataError.Storage), useCase(GAME))
        }

    private fun repository(result: AppResult<SaveOutcome>) =
        object : QuizHistoryRepository {
            override fun observeSessions(filter: HistoryFilter) = emptyFlow<List<QuizSession>>()

            override fun observePlayedCategories() = emptyFlow<List<Category>>()

            override fun observeSessionDetail(sessionId: Long) = emptyFlow<QuizSessionDetail?>()

            override fun observeQualifyingDays() = emptyFlow<List<LocalDate>>()

            override fun observeMarathonBest() = emptyFlow<Int?>()

            override fun observeGameCount() = emptyFlow<Int>()

            override suspend fun save(game: FinishedGame) = result
        }

    private companion object {
        val GAME =
            FinishedGame(
                gameUuid = "g",
                config = QuizConfig(GameMode.CLASSIC, null, "", Difficulty.ANY, QuestionType.MULTIPLE_CHOICE),
                attempts = emptyList(),
                startedAt = Instant.EPOCH,
                finishedAt = Instant.EPOCH,
                localDate = LocalDate.EPOCH,
                durationSeconds = 0,
                endReason = EndReason.COMPLETED,
            )
    }
}
