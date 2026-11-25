package sk.mpage.lumen.core.data.repository

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import sk.mpage.lumen.core.datastore.UserPreferencesDataSource
import sk.mpage.lumen.core.model.AppSettings
import sk.mpage.lumen.core.model.Difficulty
import sk.mpage.lumen.core.model.GameMode
import sk.mpage.lumen.core.model.QuestionType
import sk.mpage.lumen.core.model.QuizConfig
import sk.mpage.lumen.core.model.ThemeMode
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class DataStoreSettingsRepositoryTest {
    @get:Rule
    val folder = TemporaryFolder()

    @Test
    fun `defaults apply before anything is written`() =
        runTest {
            assertEquals(AppSettings(), repository().observeSettings().first())
        }

    @Test
    fun `every setter is read back`() =
        runTest {
            val repository = repository()
            val config = QuizConfig(GameMode.MARATHON, 9, "General Knowledge", Difficulty.HARD, QuestionType.TRUE_FALSE)

            repository.setThemeMode(ThemeMode.DARK)
            repository.setDynamicColor(true)
            repository.setReduceMotion(true)
            repository.setReduceTransparency(true)
            repository.setStreakReminders(false)
            repository.setInactivityReminders(false)
            repository.setLastQuizConfig(config)

            assertEquals(
                AppSettings(
                    themeMode = ThemeMode.DARK,
                    dynamicColor = true,
                    reduceMotion = true,
                    reduceTransparency = true,
                    streakReminders = false,
                    inactivityReminders = false,
                    lastConfig = config,
                ),
                repository.observeSettings().first(),
            )
        }

    @Test
    fun `reduce motion stores on, off and follow system`() =
        runTest {
            val repository = repository()

            repository.setReduceMotion(false)
            assertEquals(false, repository.observeSettings().first().reduceMotion)

            repository.setReduceMotion(null)
            assertNull(repository.observeSettings().first().reduceMotion)
        }

    @Test
    fun `unknown or corrupt stored values fall back to defaults`() =
        runTest {
            val source = dataSource()
            source.setThemeMode("neon")
            source.setLastQuizConfig("""{"mode":"speedrun"}""")

            val settings = DataStoreSettingsRepository(source).observeSettings().first()

            assertEquals(ThemeMode.SYSTEM, settings.themeMode)
            assertNull(settings.lastConfig)
        }

    private fun TestScope.dataSource() =
        UserPreferencesDataSource(
            PreferenceDataStoreFactory.create(
                scope = backgroundScope,
            ) { File(folder.root, "user_prefs.preferences_pb") },
        )

    private fun TestScope.repository() = DataStoreSettingsRepository(dataSource())
}
