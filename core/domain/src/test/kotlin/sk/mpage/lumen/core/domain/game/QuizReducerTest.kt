package sk.mpage.lumen.core.domain.game

import kotlinx.serialization.json.Json
import sk.mpage.lumen.core.common.result.DataError
import sk.mpage.lumen.core.domain.game.QuizPhase.Abandoned
import sk.mpage.lumen.core.domain.game.QuizPhase.Answering
import sk.mpage.lumen.core.domain.game.QuizPhase.Finished
import sk.mpage.lumen.core.domain.game.QuizPhase.LoadFailed
import sk.mpage.lumen.core.domain.game.QuizPhase.Loading
import sk.mpage.lumen.core.domain.game.QuizPhase.Presenting
import sk.mpage.lumen.core.domain.game.QuizPhase.Reveal
import sk.mpage.lumen.core.domain.game.QuizPhase.Saving
import sk.mpage.lumen.core.model.Difficulty
import sk.mpage.lumen.core.model.EndReason
import sk.mpage.lumen.core.model.GameMode
import sk.mpage.lumen.core.model.Question
import sk.mpage.lumen.core.model.QuestionType
import sk.mpage.lumen.core.model.QuizConfig
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertSame
import kotlin.test.assertTrue

class QuizReducerTest {
    private val start = QuizState(config = CONFIG, gameUuid = "game-1")

    private fun QuizState.reduce(vararg actions: QuizAction): QuizState =
        actions.fold(this) { state, action -> QuizReducer.reduce(state, action) }

    private fun answering(
        id: Int = 1,
        shownAtMs: Long = 1_000,
    ): QuizState = start.reduce(QuizAction.QuestionLoaded(question(id), id), QuizAction.QuestionReady(id, shownAtMs))

    @Test
    fun `a loaded question is presented without a timer`() {
        val state = start.reduce(QuizAction.QuestionLoaded(question(1), 1))

        assertEquals(Presenting(question(1), 1), state.phase)
        assertNull(state.firstShownAtMs)
    }

    @Test
    fun `readiness starts the 15 s deadline once`() {
        val state = answering(shownAtMs = 1_000)

        val phase = assertIs<Answering>(state.phase)
        assertEquals(1_000, phase.shownAtMs)
        assertEquals(16_000, phase.deadlineMs)
        assertEquals(1_000, state.firstShownAtMs)
        assertSame(state, state.reduce(QuizAction.QuestionReady(1, 5_000)))
    }

    @Test
    fun `readiness for another presentation is ignored`() {
        val presenting = start.reduce(QuizAction.QuestionLoaded(question(2), 2))

        assertSame(presenting, presenting.reduce(QuizAction.QuestionReady(1, 1_000)))
    }

    @Test
    fun `the first shown time is kept for later questions`() {
        val second =
            answering(shownAtMs = 1_000).reduce(
                QuizAction.AnswerSelected(CORRECT, 1, 3_000, NOW),
                QuizAction.Next,
                QuizAction.QuestionLoaded(question(2), 2),
                QuizAction.QuestionReady(2, 9_000),
            )

        assertEquals(1_000, second.firstShownAtMs)
        assertEquals(9_000, assertIs<Answering>(second.phase).shownAtMs)
    }

    @Test
    fun `an answer before the deadline records one attempt with its time`() {
        val state = answering(shownAtMs = 1_000).reduce(QuizAction.AnswerSelected(CORRECT, 1, 15_999, NOW))

        val attempt = state.attempts.single()
        assertEquals(1, attempt.position)
        assertEquals(CORRECT, attempt.selectedAnswer)
        assertTrue(attempt.isCorrect)
        assertEquals(14_999, attempt.timeSpentMs)
        assertEquals(NOW.toInstant(), attempt.answeredAt)
        assertEquals(Reveal(attempt, finished = null), state.phase)
    }

    @Test
    fun `a wrong answer is recorded as incorrect`() {
        val attempt = answering().reduce(QuizAction.AnswerSelected(WRONG, 1, 2_000, NOW)).attempts.single()

        assertEquals(WRONG, attempt.selectedAnswer)
        assertFalse(attempt.isCorrect)
    }

    @Test
    fun `a tap at the deadline counts as a timeout`() {
        val attempt =
            answering(
                shownAtMs = 1_000,
            ).reduce(QuizAction.AnswerSelected(CORRECT, 1, 16_000, NOW)).attempts.single()

        assertNull(attempt.selectedAnswer)
        assertFalse(attempt.isCorrect)
        assertEquals(15_000, attempt.timeSpentMs)
    }

    @Test
    fun `a timer tick before the deadline is ignored`() {
        val state = answering(shownAtMs = 1_000)

        assertSame(state, state.reduce(QuizAction.TimeUp(1, 15_999, NOW)))
    }

    @Test
    fun `time up at the deadline records a timeout`() {
        val state = answering(shownAtMs = 1_000).reduce(QuizAction.TimeUp(1, 16_000, NOW))

        val attempt = state.attempts.single()
        assertNull(attempt.selectedAnswer)
        assertEquals(15_000, attempt.timeSpentMs)
        assertIs<Reveal>(state.phase)
    }

    @Test
    fun `a double tap records only one attempt`() {
        val once = answering().reduce(QuizAction.AnswerSelected(CORRECT, 1, 2_000, NOW))

        assertSame(once, once.reduce(QuizAction.AnswerSelected(WRONG, 1, 2_100, NOW)))
        assertSame(once, once.reduce(QuizAction.TimeUp(1, 20_000, NOW)))
    }

    @Test
    fun `a stale presentation or an option that is not shown is ignored`() {
        val state = answering(id = 2)

        assertSame(state, state.reduce(QuizAction.AnswerSelected(CORRECT, 1, 2_000, NOW)))
        assertSame(state, state.reduce(QuizAction.TimeUp(1, 99_000, NOW)))
        assertSame(state, state.reduce(QuizAction.AnswerSelected("Not an option", 2, 2_000, NOW)))
    }

    @Test
    fun `an answer before readiness is ignored`() {
        val presenting = start.reduce(QuizAction.QuestionLoaded(question(1), 1))

        assertSame(presenting, presenting.reduce(QuizAction.AnswerSelected(CORRECT, 1, 2_000, NOW)))
    }

    @Test
    fun `next after a nonterminal reveal loads the next question`() {
        val state = answering().reduce(QuizAction.AnswerSelected(CORRECT, 1, 2_000, NOW), QuizAction.Next)

        assertEquals(Loading, state.phase)
        assertEquals(1, state.attempts.size)
    }

    @Test
    fun `a late question after the game ended is ignored`() {
        val abandoned = answering().reduce(QuizAction.QuitConfirmed)

        assertSame(abandoned, abandoned.reduce(QuizAction.QuestionLoaded(question(2), 2)))
    }

    @Test
    fun `the tenth classic answer freezes the result before feedback`() {
        val last = playClassic(answers = 10, lastNowMs = 61_500)

        val reveal = assertIs<Reveal>(last.phase)
        val game = requireNotNull(reveal.finished)
        assertEquals("game-1", game.gameUuid)
        assertEquals(10, game.attempts.size)
        assertEquals(EndReason.COMPLETED, game.endReason)
        assertEquals(60, game.durationSeconds)
        assertEquals(NOW.toInstant(), game.finishedAt)
        assertEquals(NOW.toInstant().minusMillis(60_500), game.startedAt)
        assertEquals(LocalDate.of(2026, 11, 26), game.localDate)
    }

    @Test
    fun `next after the tenth answer saves the frozen result once`() {
        val last = playClassic(answers = 10, lastNowMs = 61_500)
        val game = requireNotNull(assertIs<Reveal>(last.phase).finished)

        val saving = last.reduce(QuizAction.Next)
        assertEquals(Saving(game), saving.phase)
        assertSame(saving, saving.reduce(QuizAction.Next))
    }

    @Test
    fun `nine answers do not finish a classic game`() {
        val ninth = playClassic(answers = 9, lastNowMs = 50_000)

        assertNull(assertIs<Reveal>(ninth.phase).finished)
    }

    @Test
    fun `a feed failure can be retried`() {
        val failed = start.reduce(QuizAction.FeedFailed(DataError.NoInternet))

        assertEquals(LoadFailed(DataError.NoInternet), failed.phase)
        assertEquals(Loading, failed.reduce(QuizAction.Retry).phase)
    }

    @Test
    fun `quitting before the result is frozen abandons the game`() {
        assertEquals(Abandoned, start.reduce(QuizAction.QuitConfirmed).phase)
        assertEquals(Abandoned, answering().reduce(QuizAction.QuitConfirmed).phase)
        assertEquals(
            Abandoned,
            answering().reduce(QuizAction.AnswerSelected(CORRECT, 1, 2_000, NOW), QuizAction.QuitConfirmed).phase,
        )
    }

    @Test
    fun `quitting after the result is frozen is ignored`() {
        val last = playClassic(answers = 10, lastNowMs = 61_500)
        val saving = last.reduce(QuizAction.Next)

        assertSame(last, last.reduce(QuizAction.QuitConfirmed))
        assertSame(saving, saving.reduce(QuizAction.QuitConfirmed))
    }

    @Test
    fun `saved finishes and a save failure retries the same result`() {
        val saving = playClassic(answers = 10, lastNowMs = 61_500).reduce(QuizAction.Next)
        val game = assertIs<Saving>(saving.phase).game

        assertEquals(Finished(game, sessionId = 7, isNewBest = false), saving.reduce(QuizAction.Saved(7, false)).phase)

        val failed = saving.reduce(QuizAction.SaveFailed(DataError.Storage))
        assertEquals(QuizPhase.SaveFailed(game, DataError.Storage), failed.phase)
        assertEquals(Saving(game), failed.reduce(QuizAction.Retry).phase)
    }

    @Test
    fun `the state survives a json round trip`() {
        val state = playClassic(answers = 10, lastNowMs = 61_500)

        val json = Json.encodeToString(QuizState.serializer(), state)
        assertEquals(state, Json.decodeFromString(QuizState.serializer(), json))
    }

    @Test
    fun `a wrong marathon answer saves immediately without a reveal`() {
        val marathon = QuizState(config = CONFIG.copy(mode = GameMode.MARATHON), gameUuid = "m")
        val state =
            marathon.reduce(
                QuizAction.QuestionLoaded(question(1), 1),
                QuizAction.QuestionReady(1, 1_000),
                QuizAction.AnswerSelected(WRONG, 1, 4_000, NOW),
            )

        val game = assertIs<Saving>(state.phase).game
        assertEquals(EndReason.WRONG_ANSWER, game.endReason)
        assertEquals(3, game.durationSeconds)
        assertEquals(1, game.attempts.size)
        assertSame(state, state.reduce(QuizAction.Next))
        assertSame(state, state.reduce(QuizAction.QuitConfirmed))
    }

    @Test
    fun `a marathon timeout ends the run`() {
        val marathon = QuizState(config = CONFIG.copy(mode = GameMode.MARATHON), gameUuid = "m")
        val state =
            marathon.reduce(
                QuizAction.QuestionLoaded(question(1), 1),
                QuizAction.QuestionReady(1, 1_000),
                QuizAction.TimeUp(1, 16_000, NOW),
            )

        assertEquals(EndReason.TIMEOUT, assertIs<Saving>(state.phase).game.endReason)
    }

    @Test
    fun `a marathon keeps going past the classic length while answers are correct`() {
        val marathon = QuizState(config = CONFIG.copy(mode = GameMode.MARATHON), gameUuid = "m")
        val state =
            (1..60).fold(marathon) { current, id ->
                val loaded = if (id == 1) current else current.reduce(QuizAction.Next)
                loaded.reduce(
                    QuizAction.QuestionLoaded(question(id), id),
                    QuizAction.QuestionReady(id, id * 10_000L),
                    QuizAction.AnswerSelected(CORRECT, id, id * 10_000L + 1_000, NOW),
                )
            }

        assertEquals(60, state.attempts.size)
        assertNull(assertIs<Reveal>(state.phase).finished)
    }

    @Test
    fun `a dry marathon buffer waits, stalls on failure and can retry or quit`() {
        val marathon = QuizState(config = CONFIG.copy(mode = GameMode.MARATHON), gameUuid = "m")
        val waiting =
            marathon.reduce(
                QuizAction.QuestionLoaded(question(1), 1),
                QuizAction.QuestionReady(1, 1_000),
                QuizAction.AnswerSelected(CORRECT, 1, 2_000, NOW),
                QuizAction.Next,
            )
        assertEquals(QuizPhase.Waiting, waiting.phase)

        val stalled = waiting.reduce(QuizAction.FeedFailed(DataError.NoInternet))
        assertEquals(QuizPhase.Stalled(DataError.NoInternet), stalled.phase)
        assertEquals(QuizPhase.Waiting, stalled.reduce(QuizAction.Retry).phase)
        assertEquals(Abandoned, stalled.reduce(QuizAction.QuitConfirmed).phase)
        assertIs<Presenting>(waiting.reduce(QuizAction.QuestionLoaded(question(2), 2)).phase)
        assertEquals(1, stalled.attempts.size)
    }

    private fun playClassic(
        answers: Int,
        lastNowMs: Long,
    ): QuizState =
        (1..answers).fold(start) { state, id ->
            val shownAtMs = 1_000L + (id - 1) * 6_000
            val nowMs = if (id == answers) lastNowMs else shownAtMs + 2_000
            val loaded = if (id == 1) state else state.reduce(QuizAction.Next)
            loaded.reduce(
                QuizAction.QuestionLoaded(question(id), id),
                QuizAction.QuestionReady(id, shownAtMs),
                QuizAction.AnswerSelected(CORRECT, id, nowMs, NOW),
            )
        }

    private companion object {
        const val CORRECT = "Paris"
        const val WRONG = "Rome"
        val NOW: ZonedDateTime = ZonedDateTime.of(2026, 11, 26, 23, 30, 0, 0, ZoneId.of("Europe/Bratislava"))
        val CONFIG = QuizConfig(GameMode.CLASSIC, null, "", Difficulty.ANY, QuestionType.MULTIPLE_CHOICE)

        fun question(id: Int) =
            Question(
                id = "q$id",
                text = "Capital of France? ($id)",
                options = listOf("Berlin", CORRECT, "Madrid", WRONG),
                correctAnswer = CORRECT,
                categoryName = "Geography",
                difficulty = Difficulty.EASY,
                type = QuestionType.MULTIPLE_CHOICE,
            )
    }
}
