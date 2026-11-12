package sk.mpage.lumen.core.testing

import kotlinx.coroutines.test.TestCoroutineScheduler
import sk.mpage.lumen.core.common.time.MonotonicClock

public class TestMonotonicClock(
    private val scheduler: TestCoroutineScheduler,
) : MonotonicClock {
    override fun elapsedRealtimeMs(): Long = scheduler.currentTime
}
