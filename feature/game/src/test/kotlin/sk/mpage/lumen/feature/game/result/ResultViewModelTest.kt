package sk.mpage.lumen.feature.game.result

import app.cash.turbine.test
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import sk.mpage.lumen.core.domain.usecase.BuildShareSummaryUseCase
import sk.mpage.lumen.core.domain.usecase.CalculateStreakUseCase
import sk.mpage.lumen.core.domain.usecase.ObserveStreakUseCase
import sk.mpage.lumen.core.model.Difficulty
import sk.mpage.lumen.core.model.EndReason
import sk.mpage.lumen.core.model.GameMode
import sk.mpage.lumen.core.model.QuestionType
import sk.mpage.lumen.core.model.QuizConfig
import sk.mpage.lumen.core.model.QuizSession
import sk.mpage.lumen.core.model.QuizSessionDetail
import sk.mpage.lumen.core.testing.MainDispatcherRule
import sk.mpage.lumen.core.testing.repository.FakeQuizHistoryRepository
import sk.mpage.lumen.core.testing.repository.FakeUserProfileRepository
import sk.mpage.lumen.core.testing.time.FakeTodayProvider
import java.time.Instant
import java.time.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ResultViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val history = FakeQuizHistoryRepository()

    @Test
    fun `beating the previous marathon best is celebrated`() =
        runTest {
            show(session(1, GameMode.MARATHON, correct = 17), session(2, GameMode.MARATHON, correct = 23))
            val content = content(sessionId = 2)

            assertTrue(content.isNewBest)
            assertEquals(17, content.previousBest)
        }

    @Test
    fun `the first marathon ever is the first record`() =
        runTest {
            show(session(1, GameMode.CLASSIC, correct = 9), session(2, GameMode.MARATHON, correct = 5))
            val content = content(sessionId = 2)

            assertTrue(content.isNewBest)
            assertNull(content.previousBest)
        }

    @Test
    fun `reopening an older marathon compares with the best at that time`() =
        runTest {
            show(
                session(1, GameMode.MARATHON, correct = 10),
                session(2, GameMode.MARATHON, correct = 12),
                session(3, GameMode.MARATHON, correct = 30),
            )
            val content = content(sessionId = 2)

            assertTrue(content.isNewBest)
            assertEquals(10, content.previousBest)
        }

    @Test
    fun `a classic game is never a personal best`() =
        runTest {
            show(session(1, GameMode.CLASSIC, correct = 10))

            assertFalse(content(sessionId = 1).isNewBest)
        }

    @Test
    fun `sharing a new best says so`() =
        runTest {
            show(session(1, GameMode.MARATHON, correct = 4), session(2, GameMode.MARATHON, correct = 8))
            val viewModel = started(sessionId = 2)

            viewModel.effects.test {
                viewModel.onShareTapped()
                val effect = assertIs<ResultEffect.ShareText>(awaitItem())
                assertTrue(effect.summary.isNewBest)
            }
        }

    @Test
    fun `sharing carries the current streak`() =
        runTest {
            history.emitQualifyingDays((0L..3L).map { TODAY.minusDays(it) })
            show(session(1, GameMode.CLASSIC, correct = 9))
            val viewModel = started(sessionId = 1)

            viewModel.effects.test {
                viewModel.onShareTapped()
                assertEquals(4, assertIs<ResultEffect.ShareText>(awaitItem()).summary.streakDays)
            }
        }

    private fun show(vararg sessions: QuizSession) {
        history.emitSessions(sessions.toList())
        sessions.forEach { history.emitDetail(QuizSessionDetail(it, emptyList())) }
    }

    private fun kotlinx.coroutines.test.TestScope.started(sessionId: Long): ResultViewModel {
        val viewModel =
            ResultViewModel(
                sessionId,
                history,
                FakeUserProfileRepository(),
                BuildShareSummaryUseCase(),
                ObserveStreakUseCase(history, FakeTodayProvider(TODAY), CalculateStreakUseCase()),
            )
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.uiState.collect() }
        advanceUntilIdle()
        return viewModel
    }

    private fun kotlinx.coroutines.test.TestScope.content(sessionId: Long): ResultUiState.Content =
        assertIs(started(sessionId).uiState.value)

    private companion object {
        val TODAY: LocalDate = LocalDate.of(2026, 11, 25)
    }

    private fun session(
        id: Long,
        mode: GameMode,
        correct: Int,
    ) = QuizSession(
        id = id,
        config = QuizConfig(mode, null, "", Difficulty.ANY, QuestionType.MULTIPLE_CHOICE),
        startedAt = Instant.EPOCH,
        finishedAt = Instant.EPOCH,
        localDate = LocalDate.EPOCH,
        durationSeconds = 60,
        answerTimeMs = 30_000,
        totalQuestions = correct + 1,
        correctCount = correct,
        wrongCount = 1,
        timeoutCount = 0,
        scorePercent = correct * 100 / (correct + 1),
        averageAnswerMs = 3_000,
        endReason = EndReason.WRONG_ANSWER,
    )
}
