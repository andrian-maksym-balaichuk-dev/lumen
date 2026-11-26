package sk.mpage.lumen.core.model

import kotlinx.serialization.Serializable
import sk.mpage.lumen.core.model.serialization.InstantSerializer
import java.time.Instant

@Serializable
public data class QuestionAttempt(
    val position: Int,
    val question: Question,
    val selectedAnswer: String?,
    val isCorrect: Boolean,
    val timeSpentMs: Long,
    @Serializable(with = InstantSerializer::class) val answeredAt: Instant,
)
