package sk.mpage.lumen.core.data.time

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.shareIn
import sk.mpage.lumen.core.common.dispatcher.ApplicationScope
import sk.mpage.lumen.core.domain.port.TodayProvider
import java.time.Clock
import java.time.Duration
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class SystemTodayProvider
    @Inject
    constructor(
        clock: Clock,
        timeZoneMonitor: TimeZoneMonitor,
        @ApplicationScope appScope: CoroutineScope,
    ) : TodayProvider {
        @OptIn(ExperimentalCoroutinesApi::class)
        override val today: SharedFlow<LocalDate> =
            timeZoneMonitor.zones
                .flatMapLatest { zone ->
                    flow {
                        while (true) {
                            val now = clock.instant().atZone(zone)
                            val date = now.toLocalDate()
                            emit(date)
                            val nextMidnight = date.plusDays(1).atStartOfDay(zone)
                            delay(Duration.between(now, nextMidnight).toMillis().coerceAtLeast(1))
                        }
                    }
                }.distinctUntilChanged()
                .shareIn(appScope, SharingStarted.WhileSubscribed(5_000), replay = 1)
    }
