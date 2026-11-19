package sk.mpage.lumen.core.data.di

import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import sk.mpage.lumen.core.common.time.MonotonicClock
import sk.mpage.lumen.core.data.time.SystemMonotonicClock
import java.time.Clock

@Module
@InstallIn(SingletonComponent::class)
internal abstract class TimeModule {
    @Binds
    abstract fun monotonicClock(clock: SystemMonotonicClock): MonotonicClock

    companion object {
        @Provides
        fun clock(): Clock = Clock.systemDefaultZone()
    }
}
