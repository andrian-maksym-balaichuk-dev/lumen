package sk.mpage.lumen.feature.game.play

import sk.mpage.lumen.core.designsystem.component.AnswerState
import sk.mpage.lumen.core.domain.game.QuizPhase
import sk.mpage.lumen.core.domain.game.QuizState
import sk.mpage.lumen.core.model.Difficulty
import sk.mpage.lumen.core.model.GameMode
import sk.mpage.lumen.core.model.Question
import sk.mpage.lumen.core.model.QuestionAttempt
import sk.mpage.lumen.core.model.QuestionType
import sk.mpage.lumen.core.model.QuizConfig
import java.time.Instant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull

class QuizStateMapperTest {
    private val base = QuizState(config = CONFIG, gameUuid = "g")

    @Test
    fun `an open question shows idle options and its position`() {
        val ui =
            assertIs<GameUiState.Playing>(
                QuizStateMapper.map(base.copy(phase = QuizPhase.Answering(QUESTION, 1, 0, 15_000))),
            )

        assertEquals(1, ui.position)
        assertEquals(10, ui.total)
        assertEquals(List(4) { AnswerState.IDLE }, ui.options.map { it.state })
        assertNull(ui.feedback)
    }

    @Test
    fun `a wrong answer marks the choice, reveals the correct one and disables the rest`() {
        val ui = reveal(selected = "Earth")

        assertEquals(
            listOf(AnswerState.DISABLED, AnswerState.REVEALED_CORRECT, AnswerState.WRONG, AnswerState.DISABLED),
            ui.options.map { it.state },
        )
        assertEquals(Feedback.Wrong("Jupiter"), ui.feedback)
    }

    @Test
    fun `a correct answer and a timeout have their own feedback`() {
        assertEquals(AnswerState.CORRECT, reveal(selected = "Jupiter").options[1].state)
        assertEquals(Feedback.Correct, reveal(selected = "Jupiter").feedback)
        assertEquals(Feedback.TimedOut("Jupiter"), reveal(selected = null).feedback)
    }

    @Test
    fun `a marathon has no total`() {
        val marathon =
            base.copy(
                config = CONFIG.copy(mode = GameMode.MARATHON),
                phase = QuizPhase.Presenting(QUESTION, 1),
            )

        assertNull(assertIs<GameUiState.Playing>(QuizStateMapper.map(marathon)).total)
    }

    private fun reveal(selected: String?): GameUiState.Playing {
        val attempt = QuestionAttempt(1, QUESTION, selected, selected == "Jupiter", 3_000, Instant.EPOCH)
        return assertIs(
            QuizStateMapper.map(base.copy(attempts = listOf(attempt), phase = QuizPhase.Reveal(attempt, null))),
        )
    }

    private companion object {
        val CONFIG = QuizConfig(GameMode.CLASSIC, null, "", Difficulty.ANY, QuestionType.MULTIPLE_CHOICE)
        val QUESTION =
            Question(
                "q",
                "Shortest day?",
                listOf("Mars", "Jupiter", "Earth", "Venus"),
                "Jupiter",
                "Science",
                Difficulty.MEDIUM,
                QuestionType.MULTIPLE_CHOICE,
            )
    }
}
