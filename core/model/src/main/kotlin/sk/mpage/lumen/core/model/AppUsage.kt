package sk.mpage.lumen.core.model

import java.time.Instant
import java.time.LocalDate

public data class AppUsage(
    val lastActiveAt: Instant? = null,
    val firstLaunchedAt: Instant? = null,
    val lastStreakReminderDate: LocalDate? = null,
    val lastInactivityReminderAt: Instant? = null,
)
