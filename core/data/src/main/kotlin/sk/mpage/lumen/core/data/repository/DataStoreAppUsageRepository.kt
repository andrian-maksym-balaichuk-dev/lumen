package sk.mpage.lumen.core.data.repository

import kotlinx.coroutines.flow.first
import sk.mpage.lumen.core.datastore.PreferenceKeys
import sk.mpage.lumen.core.datastore.UserPreferencesDataSource
import sk.mpage.lumen.core.domain.repository.AppUsageRepository
import sk.mpage.lumen.core.model.AppUsage
import java.time.Instant
import java.time.LocalDate
import javax.inject.Inject

internal class DataStoreAppUsageRepository
    @Inject
    constructor(
        private val preferences: UserPreferencesDataSource,
    ) : AppUsageRepository {
        override suspend fun usage(): AppUsage {
            val prefs = preferences.preferences.first()
            return AppUsage(
                lastActiveAt = prefs[PreferenceKeys.LAST_ACTIVE_AT]?.let(Instant::ofEpochMilli),
                firstLaunchedAt = prefs[PreferenceKeys.FIRST_LAUNCHED_AT]?.let(Instant::ofEpochMilli),
                lastStreakReminderDate =
                    prefs[PreferenceKeys.LAST_STREAK_REMINDER_DATE]?.let {
                        runCatching { LocalDate.parse(it) }.getOrNull()
                    },
                lastInactivityReminderAt =
                    prefs[PreferenceKeys.LAST_INACTIVITY_REMINDER_AT]?.let(Instant::ofEpochMilli),
            )
        }

        override suspend fun markActive(at: Instant) {
            preferences.markActive(at.toEpochMilli())
        }

        override suspend fun markStreakReminderSent(date: LocalDate) {
            preferences.setLastStreakReminderDate(date.toString())
        }

        override suspend fun markInactivityReminderSent(at: Instant) {
            preferences.setLastInactivityReminderAt(at.toEpochMilli())
        }
    }
