package sk.mpage.lumen.core.testing.time

import java.time.Clock
import java.time.Duration
import java.time.Instant
import java.time.ZoneId

public class MutableClock(
    public var now: Instant,
    private val zone: ZoneId = ZoneId.of("Europe/Bratislava"),
) : Clock() {
    public fun advanceBy(duration: Duration) {
        now += duration
    }

    override fun getZone(): ZoneId = zone

    override fun withZone(zone: ZoneId): Clock = MutableClock(now, zone)

    override fun instant(): Instant = now
}
