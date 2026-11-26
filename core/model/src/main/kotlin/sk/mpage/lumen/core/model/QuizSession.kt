package sk.mpage.lumen.core.model

import java.time.Instant
import java.time.LocalDate

public data class QuizSession(
    val id: Long,
    val config: QuizConfig,
    val startedAt: Instant,
    val finishedAt: Instant,
    val localDate: LocalDate,
    val durationSeconds: Long,
    val answerTimeMs: Long,
    val totalQuestions: Int,
    val correctCount: Int,
    val wrongCount: Int,
    val timeoutCount: Int,
    val scorePercent: Int,
    val averageAnswerMs: Long,
    val endReason: EndReason,
)

public data class QuizSessionDetail(
    val session: QuizSession,
    val attempts: List<QuestionAttempt>,
)
