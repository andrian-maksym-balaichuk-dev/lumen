package sk.mpage.lumen.core.model

import kotlinx.serialization.Serializable

@Serializable
public data class QuizConfig(
    val mode: GameMode,
    val categoryId: Int?,
    val categoryName: String,
    val difficulty: Difficulty,
    val type: QuestionType,
)
