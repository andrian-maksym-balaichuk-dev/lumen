package sk.mpage.lumen.core.model

import java.time.Instant

public data class AppUsage(
    val lastActiveAt: Instant? = null,
    val firstLaunchedAt: Instant? = null,
)
