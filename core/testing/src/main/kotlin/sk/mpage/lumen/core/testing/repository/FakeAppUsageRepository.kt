package sk.mpage.lumen.core.testing.repository

import sk.mpage.lumen.core.domain.repository.AppUsageRepository
import sk.mpage.lumen.core.model.AppUsage
import java.time.Instant

public class FakeAppUsageRepository(
    public var current: AppUsage = AppUsage(),
) : AppUsageRepository {
    override suspend fun usage(): AppUsage = current

    override suspend fun markActive(at: Instant) {
        current = current.copy(lastActiveAt = at, firstLaunchedAt = current.firstLaunchedAt ?: at)
    }
}
