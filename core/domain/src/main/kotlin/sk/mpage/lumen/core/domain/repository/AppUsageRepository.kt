package sk.mpage.lumen.core.domain.repository

import sk.mpage.lumen.core.model.AppUsage
import java.time.Instant

public interface AppUsageRepository {
    public suspend fun usage(): AppUsage

    public suspend fun markActive(at: Instant)
}
