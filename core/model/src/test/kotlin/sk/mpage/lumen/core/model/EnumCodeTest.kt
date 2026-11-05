package sk.mpage.lumen.core.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class EnumCodeTest {
    @Test
    fun `every difficulty code round-trips`() {
        Difficulty.entries.forEach { assertEquals(it, Difficulty.fromCode(it.code)) }
    }

    @Test
    fun `every question type code round-trips`() {
        QuestionType.entries.forEach { assertEquals(it, QuestionType.fromCode(it.code)) }
    }

    @Test
    fun `every game mode code round-trips`() {
        GameMode.entries.forEach { assertEquals(it, GameMode.fromCode(it.code)) }
    }

    @Test
    fun `every end reason code round-trips`() {
        EndReason.entries.forEach { assertEquals(it, EndReason.fromCode(it.code)) }
    }

    @Test
    fun `codes match the open trivia db query values`() {
        assertEquals(listOf("easy", "medium", "hard"), Difficulty.entries.drop(1).map { it.code })
        assertEquals(listOf("multiple", "boolean"), QuestionType.entries.map { it.code })
    }

    @Test
    fun `an unknown code throws`() {
        assertFailsWith<IllegalArgumentException> { Difficulty.fromCode("extreme") }
        assertFailsWith<IllegalArgumentException> { QuestionType.fromCode("") }
        assertFailsWith<IllegalArgumentException> { GameMode.fromCode("CLASSIC") }
        assertFailsWith<IllegalArgumentException> { EndReason.fromCode("quit") }
    }
}
