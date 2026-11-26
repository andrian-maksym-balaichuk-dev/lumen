package sk.mpage.lumen.core.domain.usecase

import sk.mpage.lumen.core.model.Difficulty
import sk.mpage.lumen.core.model.EndReason
import sk.mpage.lumen.core.model.GameMode
import sk.mpage.lumen.core.model.QuestionType
import sk.mpage.lumen.core.model.QuizConfig
import sk.mpage.lumen.core.model.QuizSession
import sk.mpage.lumen.core.model.ShareSummary
import java.time.Instant
import java.time.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals

class BuildShareSummaryUseCaseTest {
    @Test
    fun `both modes carry score, category and duration`() {
        GameMode.entries.forEach { mode ->
            val summary = BuildShareSummaryUseCase()(session(mode))

            assertEquals(ShareSummary(mode, 8, 10, 80, "Science: Computers", Difficulty.MEDIUM, 102), summary)
        }
    }

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
