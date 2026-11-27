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
import sk.mpage.lumen.core.model.Question
import sk.mpage.lumen.core.model.QuestionAttempt
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
    fun `a marathon above the previous best is a new best`() =
        runTest {
            val useCase = SaveQuizResultUseCase(repository(AppResult.Success(SaveOutcome(7, 17, false))))

            assertEquals(
                AppResult.Success(SaveResult(7, isNewBest = true, previousBest = 17)),
                useCase(marathon(correct = 23)),
            )
        }

    @Test
    fun `equalling the previous best is not a new best`() =
        runTest {
            val useCase = SaveQuizResultUseCase(repository(AppResult.Success(SaveOutcome(7, 23, false))))

            assertEquals(false, (useCase(marathon(correct = 23)) as AppResult.Success).value.isNewBest)
        }

    @Test
    fun `the first marathon with a correct answer is the first record`() =
        runTest {
            val useCase = SaveQuizResultUseCase(repository(AppResult.Success(SaveOutcome(1, null, false))))

            assertEquals(
                AppResult.Success(SaveResult(1, isNewBest = true, previousBest = null)),
                useCase(marathon(correct = 3)),
            )
        }

    @Test
    fun `a classic game is never a personal best`() =
        runTest {
            val useCase = SaveQuizResultUseCase(repository(AppResult.Success(SaveOutcome(1, null, false))))

            assertEquals(false, (useCase(GAME.copy(attempts = attempts(10))) as AppResult.Success).value.isNewBest)
        }

    @Test
    fun `saving the same game again does not celebrate twice`() =
        runTest {
            val useCase = SaveQuizResultUseCase(repository(AppResult.Success(SaveOutcome(7, null, true))))

            assertEquals(false, (useCase(marathon(correct = 23)) as AppResult.Success).value.isNewBest)
        }

    private fun marathon(correct: Int) =
        GAME.copy(
            config = GAME.config.copy(mode = GameMode.MARATHON),
            attempts = attempts(correct) + attempt(correct, isCorrect = false),
        )

    private fun attempts(count: Int) = List(count) { attempt(it, isCorrect = true) }

    private fun attempt(
        position: Int,
        isCorrect: Boolean,
    ) = QuestionAttempt(
        position = position,
        question =
            Question(
                id = "q$position",
                text = "Q$position",
                options = listOf("True", "False"),
                correctAnswer = "True",
                categoryName = "",
                difficulty = Difficulty.EASY,
                type = QuestionType.TRUE_FALSE,
            ),
        selectedAnswer = if (isCorrect) "True" else "False",
        isCorrect = isCorrect,
        timeSpentMs = 1_000,
        answeredAt = Instant.EPOCH,
    )

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

            override suspend fun marathonBestBefore(sessionId: Long): Int? = null

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
