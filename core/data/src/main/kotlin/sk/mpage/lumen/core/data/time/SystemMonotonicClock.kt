package sk.mpage.lumen.core.data.time

import android.os.SystemClock
import sk.mpage.lumen.core.common.time.MonotonicClock
import javax.inject.Inject

internal class SystemMonotonicClock
    @Inject
    constructor() : MonotonicClock {
        override fun elapsedRealtimeMs(): Long = SystemClock.elapsedRealtime()
    }
