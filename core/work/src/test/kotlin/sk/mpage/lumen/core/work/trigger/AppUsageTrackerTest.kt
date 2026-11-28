package sk.mpage.lumen.core.work.trigger

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import sk.mpage.lumen.core.testing.repository.FakeAppUsageRepository
import sk.mpage.lumen.core.testing.time.MutableClock
import java.time.Duration
import java.time.Instant
import kotlin.test.Test
import kotlin.test.assertEquals

class AppUsageTrackerTest {
    private val owner =
        object : LifecycleOwner {
            override val lifecycle: Lifecycle get() = error("not used")
        }

    @Test
    fun `every start of the app updates the last active time`() =
        runTest {
            val usage = FakeAppUsageRepository()
            val clock = MutableClock(Instant.parse("2026-11-26T08:00:00Z"))
            val tracker = AppUsageTracker(usage, clock, backgroundScope)

            tracker.onStart(owner)
            runCurrent()
            clock.advanceBy(Duration.ofHours(5))
            tracker.onStart(owner)
            runCurrent()

            assertEquals(Instant.parse("2026-11-26T13:00:00Z"), usage.current.lastActiveAt)
            assertEquals(Instant.parse("2026-11-26T08:00:00Z"), usage.current.firstLaunchedAt)
        }
}
