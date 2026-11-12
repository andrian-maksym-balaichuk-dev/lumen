package sk.mpage.lumen.core.network.model

public data class RemoteQuestion(
    val type: String,
    val difficulty: String,
    val category: String,
    val question: String,
    val correctAnswer: String,
    val incorrectAnswers: List<String>,
)
