package sk.mpage.lumen.core.domain.usecase

import sk.mpage.lumen.core.model.Difficulty
import sk.mpage.lumen.core.model.EndReason
import sk.mpage.lumen.core.model.GameMode
import sk.mpage.lumen.core.model.QuestionType
import sk.mpage.lumen.core.model.QuizConfig
import sk.mpage.lumen.core.model.QuizSession
import sk.mpage.lumen.core.model.ShareSummary
import sk.mpage.lumen.core.model.StreakState
import java.time.Instant
import java.time.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals

class BuildShareSummaryUseCaseTest {
    @Test
    fun `both modes carry score, category and duration`() {
        GameMode.entries.forEach { mode ->
            val summary = BuildShareSummaryUseCase()(session(mode), isNewBest = false, streak = null)

            assertEquals(ShareSummary(mode, 8, 10, 80, "Science: Computers", Difficulty.MEDIUM, 102), summary)
        }
    }

    @Test
    fun `only a marathon carries the new best`() {
        val build = BuildShareSummaryUseCase()

        assertEquals(true, build(session(GameMode.MARATHON), isNewBest = true, streak = null).isNewBest)
        assertEquals(false, build(session(GameMode.CLASSIC), isNewBest = true, streak = null).isNewBest)
    }

    @Test
    fun `a streak of two or more days is shared`() {
        val build = BuildShareSummaryUseCase()

        assertEquals(4, build(session(GameMode.CLASSIC), false, streak(4)).streakDays)
        assertEquals(null, build(session(GameMode.CLASSIC), false, streak(1)).streakDays)
        assertEquals(null, build(session(GameMode.CLASSIC), false, null).streakDays)
    }

    private fun streak(current: Int) =
        StreakState(
            current = current,
            best = current,
            playedToday = true,
            atRisk = false,
            challengeProgress = minOf(current, 7),
            challengeCompleted = current >= 7,
            challengesCompleted = current / 7,
            lastSevenDays = emptyList(),
        )

    private fun session(mode: GameMode) =
        QuizSession(
            id = 1,
            config = QuizConfig(mode, 18, "Science: Computers", Difficulty.MEDIUM, QuestionType.MULTIPLE_CHOICE),
            startedAt = Instant.EPOCH,
            finishedAt = Instant.EPOCH,
            localDate = LocalDate.EPOCH,
            durationSeconds = 102,
            answerTimeMs = 40_000,
            totalQuestions = 10,
            correctCount = 8,
            wrongCount = 2,
            timeoutCount = 0,
            scorePercent = 80,
            averageAnswerMs = 4_000,
            endReason = EndReason.COMPLETED,
        )
}
