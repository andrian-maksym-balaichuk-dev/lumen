package sk.mpage.lumen.core.data.repository

import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import sk.mpage.lumen.core.common.result.AppResult
import sk.mpage.lumen.core.database.source.QuizHistoryLocalDataSource
import sk.mpage.lumen.core.domain.repository.QuizHistoryRepository
import sk.mpage.lumen.core.domain.repository.SaveOutcome
import sk.mpage.lumen.core.model.Difficulty
import sk.mpage.lumen.core.model.EndReason
import sk.mpage.lumen.core.model.FinishedGame
import sk.mpage.lumen.core.model.GameMode
import sk.mpage.lumen.core.model.Question
import sk.mpage.lumen.core.model.QuestionAttempt
import sk.mpage.lumen.core.model.QuestionType
import sk.mpage.lumen.core.model.QuizConfig
import java.time.Instant
import java.time.LocalDate
import javax.inject.Inject
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

@HiltAndroidTest
@RunWith(RobolectricTestRunner::class)
class RoomQuizHistoryRepositoryTest {
    @get:Rule
    val hiltRule = HiltAndroidRule(this)

    @Inject
    lateinit var repository: QuizHistoryRepository

    @Inject
    lateinit var local: QuizHistoryLocalDataSource

    @Before
    fun setUp() {
        hiltRule.inject()
    }

    @Test
    fun `a finished game is stored with its totals and every attempt`() =
        runTest {
            val outcome = assertSuccess(repository.save(GAME))

            val stored = assertNotNull(local.observeSessionDetail(outcome.sessionId).first())
            with(stored.session) {
                assertEquals("game-1", gameUuid)
                assertEquals(10, totalQuestions)
                assertEquals(8, correctCount)
                assertEquals(2, wrongCount)
                assertEquals(1, timeoutCount)
                assertEquals(80, scorePercent)
                assertEquals(64, durationSeconds)
                assertEquals(8 * 3_000L + 2 * 15_000L, answerTimeMs)
                assertEquals(LocalDate.of(2026, 11, 26), localDate)
            }
            assertEquals((1..10).toList(), stored.attempts.map { it.position }.sorted())
            assertEquals(listOf("A", "B", "C", "D"), stored.attempts.first().options)
        }

    @Test
    fun `saving the same game twice keeps one session`() =
        runTest {
            val first = assertSuccess(repository.save(GAME))
            val second = assertSuccess(repository.save(GAME))

            assertEquals(first.sessionId, second.sessionId)
            assertEquals(true, second.alreadySaved)
            assertEquals(1, local.observeCount().first())
        }

    private fun assertSuccess(result: AppResult<SaveOutcome>): SaveOutcome = (result as AppResult.Success).value

    private companion object {
        val QUESTION =
            Question(
                "q",
                "Q?",
                listOf("A", "B", "C", "D"),
                "A",
                "General Knowledge",
                Difficulty.EASY,
                QuestionType.MULTIPLE_CHOICE,
            )
        val GAME =
            FinishedGame(
                gameUuid = "game-1",
                config =
                    QuizConfig(
                        GameMode.CLASSIC,
                        9,
                        "General Knowledge",
                        Difficulty.ANY,
                        QuestionType.MULTIPLE_CHOICE,
                    ),
                attempts =
                    (1..10).map { position ->
                        when (position) {
                            9 -> QuestionAttempt(position, QUESTION, "B", false, 15_000, Instant.EPOCH)
                            10 -> QuestionAttempt(position, QUESTION, null, false, 15_000, Instant.EPOCH)
                            else -> QuestionAttempt(position, QUESTION, "A", true, 3_000, Instant.EPOCH)
                        }
                    },
                startedAt = Instant.parse("2026-11-26T11:58:56Z"),
                finishedAt = Instant.parse("2026-11-26T12:00:00Z"),
                localDate = LocalDate.of(2026, 11, 26),
                durationSeconds = 64,
                endReason = EndReason.COMPLETED,
            )
    }
}
