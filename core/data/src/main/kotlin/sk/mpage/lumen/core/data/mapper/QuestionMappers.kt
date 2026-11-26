package sk.mpage.lumen.core.data.mapper

import sk.mpage.lumen.core.common.text.HtmlEntityDecoder
import sk.mpage.lumen.core.model.Difficulty
import sk.mpage.lumen.core.model.Question
import sk.mpage.lumen.core.model.QuestionType
import sk.mpage.lumen.core.network.model.RemoteQuestion
import java.security.MessageDigest
import kotlin.random.Random

internal fun RemoteQuestion.toQuestion(
    decoder: HtmlEntityDecoder,
    random: Random,
    expectedType: QuestionType,
): Question? {
    val questionType = QuestionType.entries.firstOrNull { it.code == type } ?: return null
    val questionDifficulty = Difficulty.entries.firstOrNull { it.code == difficulty } ?: return null
    val expectedOptions = if (questionType == QuestionType.TRUE_FALSE) 2 else 4
    val text = decoder.decode(question)
    val correct = decoder.decode(correctAnswer)
    val options = (incorrectAnswers.map(decoder::decode) + correct)
    val valid =
        questionType == expectedType &&
            options.size == expectedOptions &&
            options.toSet().size == options.size &&
            options.count { it == correct } == 1 &&
            text.isNotBlank()
    if (!valid) return null
    return Question(
        id = sha1("$text\u0000$correct"),
        text = text,
        options = options.shuffled(random),
        correctAnswer = correct,
        categoryName = decoder.decode(category),
        difficulty = questionDifficulty,
        type = questionType,
    )
}

private fun sha1(value: String): String =
    MessageDigest
        .getInstance("SHA-1")
        .digest(value.toByteArray())
        .joinToString("") { "%02x".format(it) }
