package sk.mpage.lumen.core.data.di

import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import sk.mpage.lumen.core.common.time.MonotonicClock
import sk.mpage.lumen.core.data.time.BroadcastTimeZoneMonitor
import sk.mpage.lumen.core.data.time.SystemMonotonicClock
import sk.mpage.lumen.core.data.time.SystemTodayProvider
import sk.mpage.lumen.core.data.time.TimeZoneMonitor
import sk.mpage.lumen.core.domain.port.TodayProvider
import java.time.Clock
import kotlin.random.Random

@Module
@InstallIn(SingletonComponent::class)
internal abstract class TimeModule {
    @Binds
    abstract fun monotonicClock(clock: SystemMonotonicClock): MonotonicClock

    @Binds
    abstract fun timeZoneMonitor(monitor: BroadcastTimeZoneMonitor): TimeZoneMonitor

    @Binds
    abstract fun todayProvider(provider: SystemTodayProvider): TodayProvider

    companion object {
        @Provides
        fun clock(): Clock = Clock.systemDefaultZone()

        @Provides
        fun random(): Random = Random.Default
    }
}
