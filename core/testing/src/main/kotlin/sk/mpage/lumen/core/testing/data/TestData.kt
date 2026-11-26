package sk.mpage.lumen.core.testing.data

import sk.mpage.lumen.core.model.Difficulty
import sk.mpage.lumen.core.model.GameMode
import sk.mpage.lumen.core.model.Question
import sk.mpage.lumen.core.model.QuestionType
import sk.mpage.lumen.core.model.QuizConfig

public object TestData {
    public val classicConfig: QuizConfig =
        QuizConfig(GameMode.CLASSIC, 9, "General Knowledge", Difficulty.ANY, QuestionType.MULTIPLE_CHOICE)

    public fun question(index: Int): Question =
        Question(
            id = "q$index",
            text = "Question $index?",
            options = listOf("Wrong $index", "Right $index", "Other $index", "Last $index"),
            correctAnswer = "Right $index",
            categoryName = "General Knowledge",
            difficulty = Difficulty.EASY,
            type = QuestionType.MULTIPLE_CHOICE,
        )

    public fun questions(count: Int): List<Question> = (1..count).map(::question)
}
