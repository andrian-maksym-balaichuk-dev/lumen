package sk.mpage.lumen.core.model

import kotlinx.serialization.Serializable

@Serializable
public data class Question(
    val id: String,
    val text: String,
    val options: List<String>,
    val correctAnswer: String,
    val categoryName: String,
    val difficulty: Difficulty,
    val type: QuestionType,
)
