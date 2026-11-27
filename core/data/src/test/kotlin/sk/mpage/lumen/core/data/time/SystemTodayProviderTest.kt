package sk.mpage.lumen.core.data.time

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.onSubscription
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import sk.mpage.lumen.core.domain.usecase.CalculateStreakUseCase
import sk.mpage.lumen.core.domain.usecase.ObserveStreakUseCase
import sk.mpage.lumen.core.model.StreakState
import sk.mpage.lumen.core.testing.repository.FakeQuizHistoryRepository
import sk.mpage.lumen.core.testing.time.MutableClock
import java.time.Duration
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class SystemTodayProviderTest {
    private val bratislava = ZoneId.of("Europe/Bratislava")
    private val clock = MutableClock(ZonedDateTime.of(2026, 11, 25, 23, 59, 0, 0, bratislava).toInstant(), bratislava)
    private val zones = FakeTimeZoneMonitor(bratislava)

    @Test
    fun `emits the next date at local midnight`() =
        runTest {
            val dates = collect(provider())

            passTime(Duration.ofSeconds(61))

            assertEquals(listOf(LocalDate.of(2026, 11, 25), LocalDate.of(2026, 11, 26)), dates)
        }

    @Test
    fun `a time zone change re-emits the date in the new zone`() =
        runTest {
            clock.now = ZonedDateTime.of(2026, 11, 26, 1, 0, 0, 0, bratislava).toInstant()
            val dates = collect(provider())

            zones.change(ZoneId.of("America/New_York"))
            runCurrent()

            assertEquals(listOf(LocalDate.of(2026, 11, 26), LocalDate.of(2026, 11, 25)), dates)
        }

    @Test
    fun `two collectors share one emission`() =
        runTest {
            val provider = provider()
            val first = collect(provider)
            val second = collect(provider)

            assertEquals(1, zones.subscriptions)
            assertEquals(first, second)
        }

    @Test
    fun `midnight turns a streak played today into at risk`() =
        runTest {
            val history = FakeQuizHistoryRepository()
            history.emitQualifyingDays(listOf(LocalDate.of(2026, 11, 25)))
            val streaks = mutableListOf<StreakState>()
            backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
                ObserveStreakUseCase(history, provider(), CalculateStreakUseCase())().collect { streaks += it }
            }
            runCurrent()

            assertTrue(streaks.last().playedToday)
            assertFalse(streaks.last().atRisk)

            passTime(Duration.ofMinutes(2))

            assertFalse(streaks.last().playedToday)
            assertTrue(streaks.last().atRisk)
        }

    private fun TestScope.provider() = SystemTodayProvider(clock, zones, backgroundScope)

    private fun TestScope.collect(provider: SystemTodayProvider): List<LocalDate> {
        val dates = mutableListOf<LocalDate>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { provider.today.collect { dates += it } }
        runCurrent()
        return dates
    }

    private fun TestScope.passTime(duration: Duration) {
        clock.advanceBy(duration)
        advanceTimeBy(duration.toMillis())
        runCurrent()
    }

    private class FakeTimeZoneMonitor(
        initial: ZoneId,
    ) : TimeZoneMonitor {
        private val changes = MutableSharedFlow<ZoneId>(replay = 1).apply { tryEmit(initial) }
        var subscriptions = 0

        override val zones: Flow<ZoneId> = changes.onSubscription { subscriptions++ }

        fun change(zone: ZoneId) {
            changes.tryEmit(zone)
        }
    }
}
