package sk.mpage.lumen.core.work.trigger

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first
import sk.mpage.lumen.core.domain.port.ReminderNotifier
import sk.mpage.lumen.core.domain.repository.AppUsageRepository
import sk.mpage.lumen.core.domain.repository.SettingsRepository
import sk.mpage.lumen.core.domain.repository.UserProfileRepository
import sk.mpage.lumen.core.domain.usecase.DecideReminderUseCase
import sk.mpage.lumen.core.domain.usecase.ObserveStreakUseCase
import sk.mpage.lumen.core.domain.usecase.ReminderInput
import sk.mpage.lumen.core.model.ReminderDecision
import java.time.Clock
import java.time.ZonedDateTime

@HiltWorker
public class ReminderWorker
    @AssistedInject
    internal constructor(
        @Assisted context: Context,
        @Assisted params: WorkerParameters,
        private val settings: SettingsRepository,
        private val profiles: UserProfileRepository,
        private val appUsage: AppUsageRepository,
        private val observeStreak: ObserveStreakUseCase,
        private val decide: DecideReminderUseCase,
        private val notifier: ReminderNotifier,
        private val clock: Clock,
    ) : CoroutineWorker(context, params) {
        override suspend fun doWork(): Result {
            val now = ZonedDateTime.now(clock)
            val usage = appUsage.usage()
            val decision =
                decide(
                    ReminderInput(
                        now = now,
                        settings = settings.observeSettings().first(),
                        streak = observeStreak().first(),
                        usage = usage,
                        lastStreakReminderDate = usage.lastStreakReminderDate,
                        lastInactivityReminderAt = usage.lastInactivityReminderAt,
                        notificationsAllowed = notifier.areNotificationsAllowed(),
                        force = inputData.getBoolean(KEY_FORCE, false),
                    ),
                )
            if (decision == ReminderDecision.None) return Result.success()
            val posted = notifier.showReminder(decision, profiles.observeProfile().first().name)
            if (posted) {
                when (decision) {
                    ReminderDecision.Inactivity -> appUsage.markInactivityReminderSent(now.toInstant())
                    else -> appUsage.markStreakReminderSent(now.toLocalDate())
                }
            }
            return Result.success()
        }

        public companion object {
            public const val KEY_FORCE: String = "force"
        }
    }
