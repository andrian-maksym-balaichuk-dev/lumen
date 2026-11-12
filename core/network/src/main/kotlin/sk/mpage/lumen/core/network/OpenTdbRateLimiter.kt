package sk.mpage.lumen.core.network

import kotlinx.coroutines.delay
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import sk.mpage.lumen.core.common.time.MonotonicClock
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class OpenTdbRateLimiter
    @Inject
    constructor(
        private val clock: MonotonicClock,
    ) {
        private val mutex = Mutex()
        private var lastCallEndedAt: Long? = null

        suspend fun <T> throttle(block: suspend () -> T): T =
            mutex.withLock {
                lastCallEndedAt?.let { last ->
                    val wait = last + MIN_INTERVAL_MS - clock.elapsedRealtimeMs()
                    if (wait > 0) delay(wait)
                }
                try {
                    block()
                } finally {
                    lastCallEndedAt = clock.elapsedRealtimeMs()
                }
            }

        companion object {
            const val MIN_INTERVAL_MS: Long = 5_100L
        }
    }
