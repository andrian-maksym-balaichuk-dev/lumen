package sk.mpage.lumen.core.database.dao

import android.database.sqlite.SQLiteConstraintException
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import sk.mpage.lumen.core.database.LumenDatabase
import sk.mpage.lumen.core.database.entity.QuestionAttemptEntity
import sk.mpage.lumen.core.database.entity.QuizSessionEntity
import sk.mpage.lumen.core.model.Difficulty
import sk.mpage.lumen.core.model.EndReason
import sk.mpage.lumen.core.model.GameMode
import sk.mpage.lumen.core.model.QuestionType
import java.time.Instant
import java.time.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

@RunWith(RobolectricTestRunner::class)
class QuizSessionDaoTest {
    private lateinit var database: LumenDatabase
    private lateinit var dao: QuizSessionDao

    @Before
    fun setUp() {
        database =
            Room
                .inMemoryDatabaseBuilder(ApplicationProvider.getApplicationContext(), LumenDatabase::class.java)
                .build()
        dao = database.quizSessionDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun `a saved game is read back with its attempts`() =
        runTest {
            val outcome = dao.insertFinishedGame(session("a"), attempts(3))

            val saved = dao.observeWithAttempts(outcome.sessionId).first()

            assertFalse(outcome.alreadySaved)
            assertEquals("a", saved?.session?.gameUuid)
            assertEquals(listOf(0, 1, 2), saved?.attempts?.map { it.position }?.sorted())
            assertTrue(saved!!.attempts.all { it.sessionId == outcome.sessionId })
            assertEquals(listOf("Paris", "Rome, Italy", "Oslo"), saved.attempts.first().options)
        }

    @Test
    fun `deleting a session removes its attempts`() =
        runTest {
            val kept = dao.insertFinishedGame(session("kept"), attempts(2)).sessionId
            val deleted = dao.insertFinishedGame(session("deleted"), attempts(3)).sessionId

            dao.delete(deleted)

            assertNull(dao.observeWithAttempts(deleted).first())
            assertEquals(2, attemptCount())
            assertEquals(
                2,
                dao
                    .observeWithAttempts(kept)
                    .first()
                    ?.attempts
                    ?.size,
            )
        }

    @Test
    fun `a duplicate attempt position aborts the whole save`() =
        runTest {
            val duplicated = attempts(2) + attempt(position = 1)

            assertFailsWith<SQLiteConstraintException> { dao.insertFinishedGame(session("a"), duplicated) }

            assertEquals(0, dao.observeCount().first())
            assertEquals(0, attemptCount())
        }

    @Test
    fun `saving the same game uuid twice returns the first id and writes nothing`() =
        runTest {
            val first = dao.insertFinishedGame(session("same", mode = GameMode.MARATHON, correct = 7), attempts(2))

            val second = dao.insertFinishedGame(session("same", mode = GameMode.MARATHON, correct = 9), attempts(2))

            assertEquals(InsertOutcome(first.sessionId, previousBest = null, alreadySaved = true), second)
            assertEquals(1, dao.observeCount().first())
            assertEquals(2, attemptCount())
            assertEquals(7, dao.observeMaxCorrect(GameMode.MARATHON).first())
        }

    @Test
    fun `previousBest is the marathon best before this game and null for classic`() =
        runTest {
            val firstMarathon =
                dao.insertFinishedGame(
                    session("m1", mode = GameMode.MARATHON, correct = 12),
                    attempts(1),
                )
            dao.insertFinishedGame(session("c1", mode = GameMode.CLASSIC, correct = 10), attempts(1))
            val secondMarathon =
                dao.insertFinishedGame(
                    session("m2", mode = GameMode.MARATHON, correct = 20),
                    attempts(1),
                )
            val classic = dao.insertFinishedGame(session("c2", mode = GameMode.CLASSIC, correct = 9), attempts(1))

            assertNull(firstMarathon.previousBest)
            assertEquals(12, secondMarathon.previousBest)
            assertNull(classic.previousBest)
            assertEquals(20, dao.observeMaxCorrect(GameMode.MARATHON).first())
        }

    @Test
    fun `qualifying days are distinct, newest first and include exactly 80 percent`() =
        runTest {
            val day1 = LocalDate.of(2026, 11, 10)
            val day2 = LocalDate.of(2026, 11, 11)
            val day3 = LocalDate.of(2026, 11, 12)
            dao.insertFinishedGame(session("a", date = day1, percent = 80), attempts(1))
            dao.insertFinishedGame(session("b", date = day1, percent = 100), attempts(1))
            dao.insertFinishedGame(session("c", date = day2, percent = 79), attempts(1))
            dao.insertFinishedGame(session("d", date = day3, percent = 90), attempts(1))

            assertEquals(listOf(day3, day1), dao.observeQualifyingDays(minPercent = 80).first())
        }

    @Test
    fun `best score orders by percent then correct count then the faster game`() =
        runTest {
            dao.insertFinishedGame(session("low", percent = 50, correct = 5), emptyList())
            dao.insertFinishedGame(session("slow", percent = 90, correct = 9).copy(durationSeconds = 300), emptyList())
            dao.insertFinishedGame(session("fast", percent = 90, correct = 9).copy(durationSeconds = 60), emptyList())

            assertEquals(listOf("fast", "slow", "low"), dao.observeBestScore(null, null).first().map { it.gameUuid })
        }

    @Test
    fun `fastest uses the total duration, not the average answer time`() =
        runTest {
            dao.insertFinishedGame(session("short-total").copy(durationSeconds = 40, avgAnswerMs = 9_000), emptyList())
            dao.insertFinishedGame(
                session("quick-answers").copy(durationSeconds = 90, avgAnswerMs = 1_000),
                emptyList(),
            )

            assertEquals(
                listOf("short-total", "quick-answers"),
                dao.observeFastest(null, null).first().map { it.gameUuid },
            )
        }

    @Test
    fun `category and mode filters narrow every sort`() =
        runTest {
            dao.insertFinishedGame(session("film").copy(categoryId = 11, categoryName = "Film"), emptyList())
            dao.insertFinishedGame(session("marathon", mode = GameMode.MARATHON), emptyList())
            dao.insertFinishedGame(session("classic"), emptyList())

            assertEquals(listOf("film"), dao.observeNewest(11, null).first().map { it.gameUuid })
            assertEquals(listOf("marathon"), dao.observeBestScore(null, GameMode.MARATHON).first().map { it.gameUuid })
            assertEquals(listOf("classic"), dao.observeFastest(9, GameMode.CLASSIC).first().map { it.gameUuid })
        }

    private fun attemptCount(): Int =
        database.openHelper.readableDatabase.query("SELECT COUNT(*) FROM question_attempt").use { cursor ->
            cursor.moveToFirst()
            cursor.getInt(0)
        }

    private fun session(
        gameUuid: String,
        mode: GameMode = GameMode.CLASSIC,
        correct: Int = 8,
        percent: Int = 80,
        date: LocalDate = LocalDate.of(2026, 11, 12),
    ) = QuizSessionEntity(
        gameUuid = gameUuid,
        mode = mode,
        categoryId = 9,
        categoryName = "General Knowledge",
        difficulty = Difficulty.ANY,
        questionType = QuestionType.MULTIPLE_CHOICE,
        startedAt = STARTED,
        finishedAt = STARTED.plusSeconds(120),
        localDate = date,
        durationSeconds = 120,
        answerTimeMs = 60_000,
        totalQuestions = 10,
        correctCount = correct,
        wrongCount = 10 - correct,
        timeoutCount = 1,
        scorePercent = percent,
        avgAnswerMs = 6_000,
        endReason = EndReason.COMPLETED,
    )

    private fun attempts(count: Int) = List(count) { attempt(position = it) }

    private fun attempt(position: Int) =
        QuestionAttemptEntity(
            position = position,
            questionText = "Capital of France?",
            categoryName = "General Knowledge",
            difficulty = Difficulty.EASY,
            type = QuestionType.MULTIPLE_CHOICE,
            options = listOf("Paris", "Rome, Italy", "Oslo"),
            correctAnswer = "Paris",
            selectedAnswer = if (position == 0) null else "Paris",
            isCorrect = position != 0,
            timeSpentMs = 5_000,
            answeredAt = STARTED.plusSeconds(position * 10L),
        )

    private companion object {
        val STARTED: Instant = Instant.ofEpochMilli(1_762_900_000_000)
    }
}
