package sk.mpage.lumen.core.model

import kotlinx.serialization.Serializable
import sk.mpage.lumen.core.model.serialization.InstantSerializer
import sk.mpage.lumen.core.model.serialization.LocalDateSerializer
import java.time.Instant
import java.time.LocalDate

@Serializable
public data class FinishedGame(
    val gameUuid: String,
    val config: QuizConfig,
    val attempts: List<QuestionAttempt>,
    @Serializable(with = InstantSerializer::class) val startedAt: Instant,
    @Serializable(with = InstantSerializer::class) val finishedAt: Instant,
    @Serializable(with = LocalDateSerializer::class) val localDate: LocalDate,
    val durationSeconds: Long,
    val endReason: EndReason,
)
