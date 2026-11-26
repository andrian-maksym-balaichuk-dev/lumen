package sk.mpage.lumen.core.ui

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import sk.mpage.lumen.core.model.Difficulty
import sk.mpage.lumen.core.model.GameMode
import sk.mpage.lumen.core.model.ShareSummary
import kotlin.test.Test
import kotlin.test.assertEquals

@RunWith(RobolectricTestRunner::class)
class ShareTextFormatterTest {
    private val resources get() = ApplicationProvider.getApplicationContext<Context>().resources

    @Test
    @Config(qualifiers = "en")
    fun `classic in english has score, category and duration`() {
        assertEquals(
            "I scored 8/10 (80%) in Science: Computers · Medium in 1:42 on Lumen.\nCan you beat me?",
            ShareTextFormatter.format(CLASSIC, resources),
        )
    }

    @Test
    @Config(qualifiers = "sk")
    fun `classic in slovak`() {
        assertEquals(
            "Výsledok v Lumen: 8/10 (80 %) v kategórii Science: Computers · Stredná za 1:42.\nPrekonáš ma?",
            ShareTextFormatter.format(CLASSIC, resources),
        )
    }

    @Test
    @Config(qualifiers = "en")
    fun `marathon includes the run length, any category and the time`() {
        assertEquals(
            "I answered 23 questions in a row in Any category · Hard in 3:05 on Lumen.\nCan you beat me?",
            ShareTextFormatter.format(MARATHON, resources),
        )
    }

    @Test
    @Config(qualifiers = "sk")
    fun `marathon in slovak uses the plural form`() {
        assertEquals(
            "Výsledok v Lumen: 23 správnych odpovedí za sebou v kategórii Ľubovoľná · Ťažká za 3:05.\nPrekonáš ma?",
            ShareTextFormatter.format(MARATHON, resources),
        )
    }

    @Test
    fun `durations over an hour show hours`() {
        assertEquals("1:02:05", formatDuration(3_725))
        assertEquals("0:09", formatDuration(9))
    }

    private companion object {
        val CLASSIC = ShareSummary(GameMode.CLASSIC, 8, 10, 80, "Science: Computers", Difficulty.MEDIUM, 102)
        val MARATHON = ShareSummary(GameMode.MARATHON, 23, 24, 95, "", Difficulty.HARD, 185)
    }
}
