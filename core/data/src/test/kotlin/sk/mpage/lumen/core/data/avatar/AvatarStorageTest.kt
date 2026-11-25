package sk.mpage.lumen.core.data.avatar

import android.content.Context
import android.graphics.BitmapFactory
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.GraphicsMode
import java.io.File
import java.io.IOException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class AvatarStorageTest {
    @get:Rule
    val folder = TemporaryFolder()

    private val context: Context = ApplicationProvider.getApplicationContext()
    private val avatars = File(context.filesDir, "avatars")

    @Test
    fun `a large photo is stored as a jpeg no larger than 512 px`() =
        runTest {
            val storage = storage()

            val fileName = storage.import(pngFile(folder.root, width = 2048, height = 1536).toString())

            val file = File(avatars, fileName)
            val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            BitmapFactory.decodeFile(file.path, bounds)
            assertTrue(fileName.matches(Regex("avatar_\\d+\\.jpg")))
            assertEquals("image/jpeg", bounds.outMimeType)
            assertEquals(512 to 384, bounds.outWidth to bounds.outHeight)
            assertEquals(listOf(fileName), avatars.list()?.toList())
        }

    @Test
    fun `a small photo is not upscaled`() =
        runTest {
            val fileName = storage().import(pngFile(folder.root, width = 200, height = 120).toString())

            val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            BitmapFactory.decodeFile(File(avatars, fileName).path, bounds)
            assertEquals(200 to 120, bounds.outWidth to bounds.outHeight)
        }

    @Test
    fun `a file that is not an image fails and leaves nothing behind`() =
        runTest {
            val storage = storage()

            assertFailsWith<IOException> { storage.import(textFile(folder.root).toString()) }

            assertTrue(avatars.list().isNullOrEmpty())
        }

    @Test
    fun `delete removes the stored file`() =
        runTest {
            val storage = storage()
            val fileName = storage.import(pngFile(folder.root, width = 64, height = 64).toString())

            storage.delete(fileName)

            assertFalse(File(avatars, fileName).exists())
        }

    private fun TestScope.storage() = AvatarStorage(context, TickingClock(), StandardTestDispatcher(testScheduler))
}
