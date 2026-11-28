package sk.mpage.lumen.core.domain.repository

import sk.mpage.lumen.core.model.AppUsage
import java.time.Instant
import java.time.LocalDate

public interface AppUsageRepository {
    public suspend fun usage(): AppUsage

    public suspend fun markActive(at: Instant)

    public suspend fun markStreakReminderSent(date: LocalDate)

    public suspend fun markInactivityReminderSent(at: Instant)
}
