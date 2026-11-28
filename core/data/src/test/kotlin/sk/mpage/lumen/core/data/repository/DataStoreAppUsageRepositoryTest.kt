package sk.mpage.lumen.core.data.repository

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import sk.mpage.lumen.core.datastore.UserPreferencesDataSource
import sk.mpage.lumen.core.model.AppUsage
import java.io.File
import java.time.Instant
import java.time.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals

class DataStoreAppUsageRepositoryTest {
    @get:Rule
    val folder = TemporaryFolder()

    @Test
    fun `nothing is known before the first start`() =
        runTest {
            assertEquals(AppUsage(), repository().usage())
        }

    @Test
    fun `the first start is kept and the last start moves on`() =
        runTest {
            val repository = repository()
            val first = Instant.parse("2026-11-20T08:00:00Z")
            val later = Instant.parse("2026-11-26T19:30:00Z")

            repository.markActive(first)
            repository.markActive(later)

            assertEquals(AppUsage(lastActiveAt = later, firstLaunchedAt = first), repository.usage())
        }

    @Test
    fun `reminder bookkeeping is read back`() =
        runTest {
            val repository = repository()
            val sent = Instant.parse("2026-11-26T12:00:00Z")

            repository.markStreakReminderSent(LocalDate.of(2026, 11, 26))
            repository.markInactivityReminderSent(sent)

            val usage = repository.usage()
            assertEquals(LocalDate.of(2026, 11, 26), usage.lastStreakReminderDate)
            assertEquals(sent, usage.lastInactivityReminderAt)
        }

    private fun TestScope.repository() =
        DataStoreAppUsageRepository(
            UserPreferencesDataSource(
                PreferenceDataStoreFactory.create(scope = backgroundScope) {
                    File(folder.root, "user_prefs.preferences_pb")
                },
            ),
        )
}
