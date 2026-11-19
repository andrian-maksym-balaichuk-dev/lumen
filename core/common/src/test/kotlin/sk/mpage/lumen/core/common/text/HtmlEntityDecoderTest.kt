package sk.mpage.lumen.core.common.text

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertSame

class HtmlEntityDecoderTest {
    private val decoder = HtmlEntityDecoder()

    @Test
    fun `decodes named quote entities`() {
        assertEquals("\"Hi\"", decoder.decode("&quot;Hi&quot;"))
    }

    @Test
    fun `decodes a decimal entity with leading zero`() {
        assertEquals("'", decoder.decode("&#039;"))
    }

    @Test
    fun `decodes hexadecimal entities in both cases`() {
        assertEquals("'", decoder.decode("&#x27;"))
        assertEquals("/", decoder.decode("&#X2F;"))
    }

    @Test
    fun `decodes a named accented letter`() {
        assertEquals("é", decoder.decode("&eacute;"))
    }

    @Test
    fun `decodes only once`() {
        assertEquals("&amp;", decoder.decode("&amp;amp;"))
    }

    @Test
    fun `leaves an unknown name unchanged`() {
        assertEquals("&unknown;", decoder.decode("&unknown;"))
    }

    @Test
    fun `decodes a code point beyond the basic multilingual plane`() {
        assertEquals("😀", decoder.decode("&#128512;"))
    }

    @Test
    fun `decodes a name that expands to two code points`() {
        assertEquals("≂̸", decoder.decode("&NotEqualTilde;"))
    }

    @Test
    fun `returns the same instance when there is nothing to decode`() {
        val text = "Which planet is largest?"

        assertSame(text, decoder.decode(text))
    }

    @Test
    fun `returns an empty string unchanged`() {
        assertEquals("", decoder.decode(""))
    }

    @Test
    fun `leaves overflowing, out of range, surrogate and zero values unchanged`() {
        val invalid = listOf("&#99999999999;", "&#x110000;", "&#xD800;", "&#0;")

        invalid.forEach { assertEquals(it, decoder.decode(it)) }
    }

    @Test
    fun `decodes entities mixed into a real question`() {
        assertEquals(
            "In \"Pokémon\", what's Pikachu's type?",
            decoder.decode("In &quot;Pok&eacute;mon&quot;, what&#039;s Pikachu&#x27;s type?"),
        )
    }

    @Test
    fun `loads the complete named table`() {
        assertEquals(2125, HtmlNamedEntities.table.size)
    }
}
