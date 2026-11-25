package sk.mpage.lumen.core.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.emptyPreferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.job
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import java.io.File
import java.io.IOException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull

class UserPreferencesDataSourceTest {
    @get:Rule
    val folder = TemporaryFolder()

    @Test
    fun `a fresh store has no values`() =
        runTest {
            val preferences = dataSource().preferences.first()

            assertNull(preferences[PreferenceKeys.PROFILE_NAME])
            assertNull(preferences[PreferenceKeys.ONBOARDED])
        }

    @Test
    fun `the profile is written in one edit and marks onboarding done`() =
        runTest {
            val source = dataSource()

            source.setProfile(name = "Ada", avatar = "preset:fox")

            val preferences = source.preferences.first()
            assertEquals("Ada", preferences[PreferenceKeys.PROFILE_NAME])
            assertEquals("preset:fox", preferences[PreferenceKeys.PROFILE_AVATAR])
            assertEquals(true, preferences[PreferenceKeys.ONBOARDED])
        }

    @Test
    fun `settings are read back after reopening the file`() =
        runTest {
            val file = File(folder.root, "user_prefs.preferences_pb")
            val writerScope = CoroutineScope(StandardTestDispatcher(testScheduler) + Job())
            val writer = dataSource(file, writerScope)
            writer.setThemeMode("dark")
            writer.setDynamicColor(true)
            writer.setReduceMotion("on")
            writer.setReduceTransparency(true)
            writer.setStreakReminders(false)
            writer.setInactivityReminders(false)
            writer.setLastQuizConfig("""{"mode":"classic"}""")
            writer.setProfileName("Grace")
            writer.setProfileAvatar("file:avatar_1.jpg")
            writerScope.coroutineContext.job.cancelAndJoin()

            val preferences = dataSource(file, backgroundScope).preferences.first()

            assertEquals("dark", preferences[PreferenceKeys.THEME_MODE])
            assertEquals(true, preferences[PreferenceKeys.DYNAMIC_COLOR])
            assertEquals("on", preferences[PreferenceKeys.REDUCE_MOTION])
            assertEquals(true, preferences[PreferenceKeys.REDUCE_TRANSPARENCY])
            assertEquals(false, preferences[PreferenceKeys.NOTIF_STREAK])
            assertEquals(false, preferences[PreferenceKeys.NOTIF_INACTIVITY])
            assertEquals("""{"mode":"classic"}""", preferences[PreferenceKeys.LAST_QUIZ_CONFIG])
            assertEquals("Grace", preferences[PreferenceKeys.PROFILE_NAME])
            assertEquals("file:avatar_1.jpg", preferences[PreferenceKeys.PROFILE_AVATAR])
        }

    @Test
    fun `an io failure while reading emits empty preferences`() =
        runTest {
            val source = UserPreferencesDataSource(failingStore(IOException("disk")))

            assertEquals(emptyPreferences(), source.preferences.first())
        }

    @Test
    fun `other read failures are not swallowed`() =
        runTest {
            val source = UserPreferencesDataSource(failingStore(IllegalStateException("bug")))

            assertFailsWith<IllegalStateException> { source.preferences.first() }
        }

    private fun TestScope.dataSource(
        file: File = File(folder.root, "user_prefs.preferences_pb"),
        scope: CoroutineScope = backgroundScope,
    ) = UserPreferencesDataSource(PreferenceDataStoreFactory.create(scope = scope) { file })

    private fun failingStore(error: Throwable) =
        object : DataStore<Preferences> {
            override val data: Flow<Preferences> = flow { throw error }

            override suspend fun updateData(transform: suspend (t: Preferences) -> Preferences): Preferences =
                throw error
        }
}
