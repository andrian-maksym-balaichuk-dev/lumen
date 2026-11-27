package sk.mpage.lumen.feature.history.detail

import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import sk.mpage.lumen.core.model.EndReason
import sk.mpage.lumen.core.model.QuestionAttempt
import sk.mpage.lumen.core.model.QuizSession
import sk.mpage.lumen.core.model.QuizSessionDetail
import sk.mpage.lumen.core.testing.MainDispatcherRule
import sk.mpage.lumen.core.testing.data.TestData
import sk.mpage.lumen.core.testing.repository.FakeQuizHistoryRepository
import java.time.Instant
import java.time.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class SessionDetailViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val history = FakeQuizHistoryRepository()

    @Test
    fun `a missing session shows not found`() =
        runTest {
            val viewModel = SessionDetailViewModel(7, history)
            backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.uiState.collect() }
            advanceUntilIdle()

            assertEquals(SessionDetailUiState.NotFound, viewModel.uiState.value)
        }

    @Test
    fun `attempts are shown in question order`() =
        runTest {
            history.emitDetail(QuizSessionDetail(session(7), listOf(attempt(3), attempt(1), attempt(2))))
            val viewModel = SessionDetailViewModel(7, history)
            backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.uiState.collect() }
            advanceUntilIdle()

            val content = assertIs<SessionDetailUiState.Content>(viewModel.uiState.value)
            assertEquals(7, content.session.id)
            assertEquals(listOf(1, 2, 3), content.attempts.map { it.position })
        }

    private val at = Instant.parse("2026-11-26T12:00:00Z")

    private fun attempt(position: Int) =
        QuestionAttempt(
            position = position,
            question = TestData.question(position),
            selectedAnswer = if (position == 3) null else "Right $position",
            isCorrect = position != 3,
            timeSpentMs = 4_000,
            answeredAt = at,
        )

    private fun session(id: Long) =
        QuizSession(
            id = id,
            config = TestData.classicConfig,
            startedAt = at,
            finishedAt = at.plusSeconds(30),
            localDate = LocalDate.of(2026, 11, 26),
            durationSeconds = 30,
            answerTimeMs = 12_000,
            totalQuestions = 3,
            correctCount = 2,
            wrongCount = 1,
            timeoutCount = 1,
            scorePercent = 67,
            averageAnswerMs = 4_000,
            endReason = EndReason.COMPLETED,
        )
}
