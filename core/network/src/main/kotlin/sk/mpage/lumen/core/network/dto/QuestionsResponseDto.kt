package sk.mpage.lumen.core.network.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class QuestionsResponseDto(
    @SerialName("response_code") val responseCode: Int,
    val results: List<QuestionDto> = emptyList(),
)

@Serializable
internal data class QuestionDto(
    val type: String,
    val difficulty: String,
    val category: String,
    val question: String,
    @SerialName("correct_answer") val correctAnswer: String,
    @SerialName("incorrect_answers") val incorrectAnswers: List<String>,
)
