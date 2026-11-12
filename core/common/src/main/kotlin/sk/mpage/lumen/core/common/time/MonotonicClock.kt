package sk.mpage.lumen.core.common.time

public fun interface MonotonicClock {
    public fun elapsedRealtimeMs(): Long
}
