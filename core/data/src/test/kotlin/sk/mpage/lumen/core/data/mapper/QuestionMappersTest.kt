package sk.mpage.lumen.core.data.mapper

import sk.mpage.lumen.core.common.text.HtmlEntityDecoder
import sk.mpage.lumen.core.model.Difficulty
import sk.mpage.lumen.core.model.QuestionType
import sk.mpage.lumen.core.network.model.RemoteQuestion
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class QuestionMappersTest {
    private val decoder = HtmlEntityDecoder()

    @Test
    fun `text and answers are decoded once`() {
        val question =
            assertNotNull(
                multiple(question = "Who wrote &quot;Hamlet&quot;?", correct = "Shakespeare &amp; Co")
                    .toQuestion(decoder, Random(1), QuestionType.MULTIPLE_CHOICE),
            )

        assertEquals("Who wrote \"Hamlet\"?", question.text)
        assertEquals("Shakespeare & Co", question.correctAnswer)
        assertEquals("Entertainment: Books", question.categoryName)
        assertEquals(Difficulty.EASY, question.difficulty)
    }

    @Test
    fun `options contain the correct answer exactly once`() {
        repeat(20) { seed ->
            val question = assertNotNull(multiple().toQuestion(decoder, Random(seed), QuestionType.MULTIPLE_CHOICE))

            assertEquals(4, question.options.size)
            assertEquals(1, question.options.count { it == question.correctAnswer })
        }
    }

    @Test
    fun `true or false options are shuffled too`() {
        val orders =
            (0 until 20)
                .map { seed ->
                    assertNotNull(boolean().toQuestion(decoder, Random(seed), QuestionType.TRUE_FALSE)).options
                }.toSet()

        assertEquals(setOf(listOf("True", "False"), listOf("False", "True")), orders)
    }

    @Test
    fun `the id is stable for the same text and answer`() {
        val first = multiple().toQuestion(decoder, Random(1), QuestionType.MULTIPLE_CHOICE)
        val second = multiple().toQuestion(decoder, Random(2), QuestionType.MULTIPLE_CHOICE)

        assertEquals(first?.id, second?.id)
    }

    @Test
    fun `malformed questions are rejected`() {
        val type = QuestionType.MULTIPLE_CHOICE
        assertNull(multiple(incorrect = listOf("B", "C")).toQuestion(decoder, Random(1), type))
        assertNull(multiple(incorrect = listOf("A", "C", "D")).toQuestion(decoder, Random(1), type))
        assertNull(boolean().toQuestion(decoder, Random(1), type))
        assertNull(multiple().copy(difficulty = "legendary").toQuestion(decoder, Random(1), type))
    }

    private fun multiple(
        question: String = "Q?",
        correct: String = "A",
        incorrect: List<String> = listOf("B", "C", "D"),
    ) = RemoteQuestion("multiple", "easy", "Entertainment: Books", question, correct, incorrect)

    private fun boolean() =
        RemoteQuestion("boolean", "medium", "Science & Nature", "Water is wet.", "True", listOf("False"))
}
