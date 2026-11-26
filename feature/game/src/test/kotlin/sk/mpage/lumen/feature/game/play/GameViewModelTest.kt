package sk.mpage.lumen.feature.game.play

import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import sk.mpage.lumen.core.common.result.DataError
import sk.mpage.lumen.core.domain.usecase.SaveQuizResultUseCase
import sk.mpage.lumen.core.testing.MainDispatcherRule
import sk.mpage.lumen.core.testing.TestMonotonicClock
import sk.mpage.lumen.core.testing.data.TestData
import sk.mpage.lumen.core.testing.feed.FakeQuestionFeed
import sk.mpage.lumen.core.testing.repository.FakeQuestionRepository
import sk.mpage.lumen.core.testing.repository.FakeQuizHistoryRepository
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue

class GameViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val feed = FakeQuestionFeed(TestData.questions(10))
    private val history = FakeQuizHistoryRepository()

    private fun TestScope.viewModel(): GameViewModel =
        GameViewModel(
            config = TestData.classicConfig,
            questions = FakeQuestionRepository(feed),
            saveResult = SaveQuizResultUseCase(history),
            monotonicClock = TestMonotonicClock(testScheduler),
            clock = Clock.fixed(Instant.parse("2026-11-26T12:00:00Z"), ZoneOffset.UTC),
        ).also { runCurrent() }

    private fun GameViewModel.playing(): GameUiState.Playing = assertIs(uiState.value)

    private fun TestScope.show(viewModel: GameViewModel) {
        viewModel.onAction(GameAction.QuestionShown(viewModel.playing().presentationId))
        runCurrent()
    }

    @Test
    fun `an answer is revealed and the next question follows after the reveal`() =
        runTest {
            val viewModel = viewModel()
            show(viewModel)

            viewModel.onAction(GameAction.OptionTapped("Right 1", 1))
            runCurrent()
            assertEquals(Feedback.Correct, viewModel.playing().feedback)

            advanceTimeBy(1_199)
            runCurrent()
            assertEquals(1, viewModel.playing().position)

            advanceTimeBy(2)
            runCurrent()
            assertEquals(2, viewModel.playing().position)
            assertNull(viewModel.playing().feedback)
        }

    @Test
    fun `an unanswered question times out after 15 s`() =
        runTest {
            val viewModel = viewModel()
            show(viewModel)

            advanceTimeBy(14_999)
            runCurrent()
            assertNull(viewModel.playing().feedback)

            advanceTimeBy(2)
            runCurrent()
            assertEquals(Feedback.TimedOut("Right 1"), viewModel.playing().feedback)
            assertEquals(0, viewModel.remainingMs.value)
        }

    @Test
    fun `the timer does not run before the question is shown`() =
        runTest {
            val viewModel = viewModel()

            advanceTimeBy(30_000)
            runCurrent()
            assertNull(viewModel.playing().feedback)
            assertEquals(15_000, viewModel.remainingMs.value)
        }

    @Test
    fun `the reveal waits while the game is not visible`() =
        runTest {
            val viewModel = viewModel()
            show(viewModel)
            viewModel.onAction(GameAction.VisibilityChanged(false))
            viewModel.onAction(GameAction.OptionTapped("Wrong 1", 1))

            advanceTimeBy(10_000)
            runCurrent()
            assertEquals(Feedback.Wrong("Right 1"), viewModel.playing().feedback)

            viewModel.onAction(GameAction.VisibilityChanged(true))
            advanceTimeBy(1_201)
            runCurrent()
            assertEquals(2, viewModel.playing().position)
        }

    @Test
    fun `a question shown while hidden starts its timer when visible again`() =
        runTest {
            val viewModel = viewModel()
            viewModel.onAction(GameAction.VisibilityChanged(false))
            show(viewModel)

            advanceTimeBy(20_000)
            runCurrent()
            assertNull(viewModel.playing().feedback)

            viewModel.onAction(GameAction.VisibilityChanged(true))
            advanceTimeBy(15_001)
            runCurrent()
            assertEquals(Feedback.TimedOut("Right 1"), viewModel.playing().feedback)
        }

    @Test
    fun `a failed load can be retried`() =
        runTest {
            feed.failNext(DataError.NoInternet)
            val viewModel = viewModel()
            assertEquals(GameUiState.LoadFailed(DataError.NoInternet), viewModel.uiState.value)

            viewModel.onAction(GameAction.RetryTapped)
            runCurrent()
            assertEquals(1, viewModel.playing().position)
        }

    private fun TestScope.playTen(viewModel: GameViewModel) {
        repeat(10) { index ->
            show(viewModel)
            advanceTimeBy(2_000)
            viewModel.onAction(
                GameAction.OptionTapped(
                    if (index <
                        8
                    ) {
                        "Right ${index + 1}"
                    } else {
                        "Wrong ${index + 1}"
                    },
                    index + 1,
                ),
            )
            advanceTimeBy(1_201)
            runCurrent()
        }
    }

    @Test
    fun `ten answers save the game once`() =
        runTest {
            val viewModel = viewModel()
            playTen(viewModel)

            val game = history.saved.single()
            assertEquals(10, game.totalQuestions)
            assertEquals(8, game.correctCount)
            assertEquals(80, game.scorePercent)
            assertEquals(2_000L * 10, game.answerTimeMs)
            assertTrue(feed.closed)
        }

    @Test
    fun `a failed save can be retried with the same result`() =
        runTest {
            history.failNext(DataError.Storage)
            val viewModel = viewModel()
            playTen(viewModel)
            assertEquals(GameUiState.SaveFailed(DataError.Storage), viewModel.uiState.value)

            viewModel.onAction(GameAction.RetryTapped)
            runCurrent()
            assertEquals(1, history.saved.size)
        }
}
