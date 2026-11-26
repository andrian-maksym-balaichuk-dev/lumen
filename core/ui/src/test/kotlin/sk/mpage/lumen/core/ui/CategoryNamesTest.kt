package sk.mpage.lumen.core.ui

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.test.Test
import kotlin.test.assertEquals

@RunWith(RobolectricTestRunner::class)
class CategoryNamesTest {
    private val resources get() = ApplicationProvider.getApplicationContext<Context>().resources

    @Test
    @Config(qualifiers = "sk")
    fun `api names are translated by their category`() {
        assertEquals("Počítače", categoryDisplayName("Science: Computers", resources))
        assertEquals("Všeobecné vedomosti", categoryDisplayName("General Knowledge", resources))
    }

    @Test
    @Config(qualifiers = "en")
    fun `unknown api names keep their short english name`() {
        assertEquals("Board Games", categoryDisplayName("Entertainment: Board Games", resources))
        assertEquals("Space", categoryDisplayName("Science: Space", resources))
    }
}
