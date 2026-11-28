package sk.mpage.lumen.core.data.repository

import kotlinx.coroutines.flow.first
import sk.mpage.lumen.core.datastore.PreferenceKeys
import sk.mpage.lumen.core.datastore.UserPreferencesDataSource
import sk.mpage.lumen.core.domain.repository.AppUsageRepository
import sk.mpage.lumen.core.model.AppUsage
import java.time.Instant
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
            )
        }

        override suspend fun markActive(at: Instant) {
            preferences.markActive(at.toEpochMilli())
        }
    }
