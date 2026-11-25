package sk.mpage.lumen.core.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.GraphicsMode
import sk.mpage.lumen.core.common.result.AppResult
import sk.mpage.lumen.core.common.result.DataError
import sk.mpage.lumen.core.data.avatar.AvatarStorage
import sk.mpage.lumen.core.data.avatar.TickingClock
import sk.mpage.lumen.core.data.avatar.pngFile
import sk.mpage.lumen.core.data.avatar.textFile
import sk.mpage.lumen.core.datastore.UserPreferencesDataSource
import sk.mpage.lumen.core.model.Avatar
import sk.mpage.lumen.core.model.AvatarInput
import sk.mpage.lumen.core.model.UserProfile
import java.io.File
import java.io.IOException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotEquals

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class DataStoreUserProfileRepositoryTest {
    @get:Rule
    val folder = TemporaryFolder()

    private val context: Context = ApplicationProvider.getApplicationContext()
    private val avatars = File(context.filesDir, "avatars")

    @Test
    fun `a fresh install has an empty profile that is not onboarded`() =
        runTest {
            assertEquals(
                UserProfile(name = "", avatar = Avatar.None, onboarded = false),
                repository().observeProfile().first(),
            )
        }

    @Test
    fun `completing onboarding stores the name and preset avatar`() =
        runTest {
            val repository = repository()

            val result = repository.completeOnboarding("Ada", Avatar.Preset("fox"))

            assertEquals(AppResult.Success(Unit), result)
            assertEquals(
                UserProfile("Ada", Avatar.Preset("fox"), onboarded = true),
                repository.observeProfile().first(),
            )
        }

    @Test
    fun `renaming keeps the avatar`() =
        runTest {
            val repository = repository()
            repository.completeOnboarding("Ada", Avatar.Preset("fox"))

            repository.updateName("Grace")

            assertEquals(
                UserProfile("Grace", Avatar.Preset("fox"), onboarded = true),
                repository.observeProfile().first(),
            )
        }

    @Test
    fun `avatar strings round-trip for presets and files`() {
        listOf(Avatar.Preset("owl"), Avatar.Custom("avatar_1.jpg"), Avatar.None).forEach {
            assertEquals(it, decodeAvatar(encodeAvatar(it)))
        }
        assertEquals("preset:owl", encodeAvatar(Avatar.Preset("owl")))
        assertEquals("file:avatar_1.jpg", encodeAvatar(Avatar.Custom("avatar_1.jpg")))
        listOf("", "preset:", "file:", "gif:cat").forEach { assertEquals(Avatar.None, decodeAvatar(it)) }
    }

    @Test
    fun `a picked photo replaces the previous photo file`() =
        runTest {
            val repository = repository()
            val first =
                assertIs<Avatar.Custom>(repository.pickedValue(pngFile(folder.root, 800, 600, "a.png").toString()))

            val second =
                assertIs<Avatar.Custom>(repository.pickedValue(pngFile(folder.root, 600, 800, "b.png").toString()))

            assertEquals(second, repository.observeProfile().first().avatar)
            assertEquals(listOf(second.fileName), avatars.list()?.toList())
            assertNotEquals(first, second)
        }

    @Test
    fun `switching to a preset deletes the photo file`() =
        runTest {
            val repository = repository()
            repository.pickedValue(pngFile(folder.root, 64, 64).toString())

            val result = repository.updateAvatar(AvatarInput.Preset("cat"))

            assertEquals(AppResult.Success(Avatar.Preset("cat")), result)
            assertEquals(emptyList(), avatars.list()?.toList())
        }

    @Test
    fun `finishing onboarding with a preset deletes a photo picked earlier`() =
        runTest {
            val repository = repository()
            repository.pickedValue(pngFile(folder.root, 64, 64).toString())

            repository.completeOnboarding("Ada", Avatar.Preset("owl"))

            assertEquals(
                UserProfile("Ada", Avatar.Preset("owl"), onboarded = true),
                repository.observeProfile().first(),
            )
            assertEquals(emptyList(), avatars.list()?.toList())
        }

    @Test
    fun `finishing onboarding with the picked photo keeps its file`() =
        runTest {
            val repository = repository()
            val photo = repository.pickedValue(pngFile(folder.root, 64, 64).toString()) as Avatar.Custom

            repository.completeOnboarding("Ada", photo)

            assertEquals(photo, repository.observeProfile().first().avatar)
            assertEquals(listOf(photo.fileName), avatars.list()?.toList())
        }

    @Test
    fun `an unreadable photo fails and keeps the old avatar`() =
        runTest {
            val repository = repository()
            val old = repository.pickedValue(pngFile(folder.root, 64, 64).toString()) as Avatar.Custom

            val result = repository.updateAvatar(AvatarInput.Picked(textFile(folder.root).toString()))

            assertEquals(AppResult.Failure(DataError.Storage), result)
            assertEquals(old, repository.observeProfile().first().avatar)
            assertEquals(listOf(old.fileName), avatars.list()?.toList())
        }

    @Test
    fun `a failed save removes the new file and keeps the old avatar`() =
        runTest {
            val store = ControllableStore(realStore())
            val repository = repository(store)
            val old = repository.pickedValue(pngFile(folder.root, 64, 64, "old.png").toString()) as Avatar.Custom
            store.failWrites = true

            val result = repository.updateAvatar(AvatarInput.Picked(pngFile(folder.root, 64, 64, "new.png").toString()))

            assertEquals(AppResult.Failure(DataError.Storage), result)
            assertEquals(old, repository.observeProfile().first().avatar)
            assertEquals(listOf(old.fileName), avatars.list()?.toList())
        }

    @Test
    fun `cancelling during the save removes the new file and keeps the old avatar`() =
        runTest {
            val store = ControllableStore(realStore())
            val repository = repository(store)
            val old = repository.pickedValue(pngFile(folder.root, 64, 64, "old.png").toString()) as Avatar.Custom
            store.hangWrites = true

            val update =
                launch {
                    repository.updateAvatar(
                        AvatarInput.Picked(pngFile(folder.root, 64, 64, "new.png").toString()),
                    )
                }
            store.writeStarted.await()
            update.cancelAndJoin()

            assertEquals(old, repository.observeProfile().first().avatar)
            assertEquals(listOf(old.fileName), avatars.list()?.toList())
        }

    private suspend fun DataStoreUserProfileRepository.pickedValue(uri: String): Avatar =
        (updateAvatar(AvatarInput.Picked(uri)) as AppResult.Success).value

    private fun TestScope.realStore(): DataStore<Preferences> =
        PreferenceDataStoreFactory.create(scope = backgroundScope) { File(folder.root, "user_prefs.preferences_pb") }

    private fun TestScope.repository(store: DataStore<Preferences> = realStore()) =
        DataStoreUserProfileRepository(
            UserPreferencesDataSource(store),
            AvatarStorage(context, TickingClock(), StandardTestDispatcher(testScheduler)),
        )

    private class ControllableStore(
        private val delegate: DataStore<Preferences>,
    ) : DataStore<Preferences> {
        var failWrites = false
        var hangWrites = false
        val writeStarted = CompletableDeferred<Unit>()

        override val data: Flow<Preferences> = delegate.data

        override suspend fun updateData(transform: suspend (t: Preferences) -> Preferences): Preferences {
            if (failWrites) throw IOException("disk full")
            if (hangWrites) {
                writeStarted.complete(Unit)
                awaitCancellation()
            }
            return delegate.updateData(transform)
        }
    }
}
