package sk.mpage.lumen.core.network

import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import sk.mpage.lumen.core.network.OpenTdbRateLimiter.Companion.MIN_INTERVAL_MS
import sk.mpage.lumen.core.testing.TestMonotonicClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class OpenTdbRateLimiterTest {
    @Test
    fun `the first call starts immediately`() =
        runTest {
            val limiter = OpenTdbRateLimiter(TestMonotonicClock(testScheduler))

            limiter.throttle { assertEquals(0, testScheduler.currentTime) }
        }

    @Test
    fun `the second call starts no earlier than the interval after the first ended`() =
        runTest {
            val limiter = OpenTdbRateLimiter(TestMonotonicClock(testScheduler))

            limiter.throttle { delay(300) }
            val firstEnded = testScheduler.currentTime
            val secondStarted = limiter.throttle { testScheduler.currentTime }

            assertEquals(firstEnded + MIN_INTERVAL_MS, secondStarted)
        }

    @Test
    fun `no wait is added when enough time has already passed`() =
        runTest {
            val limiter = OpenTdbRateLimiter(TestMonotonicClock(testScheduler))

            limiter.throttle { }
            delay(MIN_INTERVAL_MS + 1_000)
            val before = testScheduler.currentTime
            val started = limiter.throttle { testScheduler.currentTime }

            assertEquals(before, started)
        }

    @Test
    fun `concurrent calls never overlap and keep the interval`() =
        runTest {
            val limiter = OpenTdbRateLimiter(TestMonotonicClock(testScheduler))
            val spans = mutableListOf<LongRange>()
            var active = 0
            var maxActive = 0

            repeat(3) {
                launch {
                    limiter.throttle {
                        active++
                        maxActive = maxOf(maxActive, active)
                        val start = testScheduler.currentTime
                        delay(1_000)
                        spans += start..testScheduler.currentTime
                        active--
                    }
                }
            }
            testScheduler.advanceUntilIdle()

            assertEquals(1, maxActive)
            assertEquals(3, spans.size)
            spans.zipWithNext().forEach { (previous, next) ->
                assertTrue(next.first >= previous.last + MIN_INTERVAL_MS, "$previous then $next")
            }
        }

    @Test
    fun `a failed call still counts for the interval`() =
        runTest {
            val limiter = OpenTdbRateLimiter(TestMonotonicClock(testScheduler))

            assertFailsWith<IllegalStateException> { limiter.throttle { error("network bug") } }
            val failedAt = testScheduler.currentTime
            val nextStarted = limiter.throttle { testScheduler.currentTime }

            assertEquals(failedAt + MIN_INTERVAL_MS, nextStarted)
        }
}
