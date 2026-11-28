package sk.mpage.lumen.core.work.trigger

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.work.ListenableWorker
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import androidx.work.testing.TestListenableWorkerBuilder
import androidx.work.workDataOf
import kotlinx.coroutines.test.runTest
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import sk.mpage.lumen.core.domain.usecase.CalculateStreakUseCase
import sk.mpage.lumen.core.domain.usecase.DecideReminderUseCase
import sk.mpage.lumen.core.domain.usecase.ObserveStreakUseCase
import sk.mpage.lumen.core.model.AppUsage
import sk.mpage.lumen.core.model.Avatar
import sk.mpage.lumen.core.model.ReminderDecision
import sk.mpage.lumen.core.model.UserProfile
import sk.mpage.lumen.core.testing.port.RecordingNotifier
import sk.mpage.lumen.core.testing.repository.FakeAppUsageRepository
import sk.mpage.lumen.core.testing.repository.FakeQuizHistoryRepository
import sk.mpage.lumen.core.testing.repository.FakeSettingsRepository
import sk.mpage.lumen.core.testing.repository.FakeUserProfileRepository
import sk.mpage.lumen.core.testing.time.FakeTodayProvider
import sk.mpage.lumen.core.testing.time.MutableClock
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

@RunWith(RobolectricTestRunner::class)
class ReminderWorkerTest {
    private val context: Context = ApplicationProvider.getApplicationContext()
    private val zone = ZoneId.of("Europe/Bratislava")
    private val today = LocalDate.of(2026, 11, 25)
    private val history = FakeQuizHistoryRepository()
    private val notifier = RecordingNotifier()
    private val usage = FakeAppUsageRepository(AppUsage(lastActiveAt = at(12, 0).toInstant()))

    @Test
    fun `an evening without a game today posts the streak reminder once`() =
        runTest {
            history.emitQualifyingDays(listOf(today.minusDays(1), today.minusDays(2)))

            assertEquals(ListenableWorker.Result.success(), worker(at(18, 0)).doWork())
            worker(at(19, 0)).doWork()

            assertEquals(listOf<ReminderDecision>(ReminderDecision.StreakAtRisk(2)), notifier.reminders)
            assertEquals(today, usage.current.lastStreakReminderDate)
        }

    @Test
    fun `no streak yet asks to start the challenge`() =
        runTest {
            worker(at(18, 0)).doWork()

            assertEquals(listOf<ReminderDecision>(ReminderDecision.StartChallenge), notifier.reminders)
        }

    @Test
    fun `force bypasses quiet hours and the evening window`() =
        runTest {
            worker(at(3, 0)).doWork()
            assertEquals(emptyList(), notifier.reminders)

            worker(at(3, 0), force = true).doWork()

            assertEquals(listOf<ReminderDecision>(ReminderDecision.StartChallenge), notifier.reminders)
        }

    @Test
    fun `a reminder that could not be posted is not recorded`() =
        runTest {
            notifier.posts = false

            assertEquals(ListenableWorker.Result.success(), worker(at(18, 0)).doWork())

            assertNull(usage.current.lastStreakReminderDate)
        }

    @Test
    fun `three days away posts a come-back reminder`() =
        runTest {
            usage.current = AppUsage(lastActiveAt = at(12, 0).minusDays(4).toInstant())

            worker(at(12, 0)).doWork()

            assertEquals(listOf<ReminderDecision>(ReminderDecision.Inactivity), notifier.reminders)
            assertEquals(at(12, 0).toInstant(), usage.current.lastInactivityReminderAt)
        }

    private fun at(
        hour: Int,
        minute: Int,
    ): ZonedDateTime = ZonedDateTime.of(today, LocalTime.of(hour, minute), zone)

    private fun worker(
        now: ZonedDateTime,
        force: Boolean = false,
    ): ReminderWorker =
        TestListenableWorkerBuilder<ReminderWorker>(context)
            .setInputData(workDataOf(ReminderWorker.KEY_FORCE to force))
            .setWorkerFactory(
                object : WorkerFactory() {
                    override fun createWorker(
                        appContext: Context,
                        workerClassName: String,
                        workerParameters: WorkerParameters,
                    ) = ReminderWorker(
                        appContext,
                        workerParameters,
                        FakeSettingsRepository(),
                        FakeUserProfileRepository(UserProfile("Ada", Avatar.None, onboarded = true)),
                        usage,
                        ObserveStreakUseCase(history, FakeTodayProvider(now.toLocalDate()), CalculateStreakUseCase()),
                        DecideReminderUseCase(),
                        notifier,
                        MutableClock(now.toInstant(), zone),
                    )
                },
            ).build()
}
