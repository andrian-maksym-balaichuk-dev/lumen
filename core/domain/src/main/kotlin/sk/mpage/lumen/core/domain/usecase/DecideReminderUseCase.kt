package sk.mpage.lumen.core.domain.usecase

import sk.mpage.lumen.core.model.AppSettings
import sk.mpage.lumen.core.model.AppUsage
import sk.mpage.lumen.core.model.ReminderDecision
import sk.mpage.lumen.core.model.StreakState
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZonedDateTime
import javax.inject.Inject

public data class ReminderInput(
    val now: ZonedDateTime,
    val settings: AppSettings,
    val streak: StreakState,
    val usage: AppUsage,
    val lastStreakReminderDate: LocalDate?,
    val lastInactivityReminderAt: Instant?,
    val notificationsAllowed: Boolean,
    val force: Boolean,
)

public class DecideReminderUseCase
    @Inject
    constructor() {
        public operator fun invoke(input: ReminderInput): ReminderDecision {
            if (!input.notificationsAllowed) return ReminderDecision.None
            val time = input.now.toLocalTime()
            if (!input.force && time.isQuiet()) return ReminderDecision.None
            if (input.settings.inactivityReminders && input.isInactive() && input.canRemindInactivity()) {
                return ReminderDecision.Inactivity
            }
            if (input.settings.streakReminders && !input.streak.playedToday && input.canRemindStreak(time)) {
                return if (input.streak.current > 0) {
                    ReminderDecision.StreakAtRisk(input.streak.current)
                } else {
                    ReminderDecision.StartChallenge
                }
            }
            return ReminderDecision.None
        }

        private fun LocalTime.isQuiet(): Boolean = this >= QUIET_START || this < QUIET_END

        private fun ReminderInput.isInactive(): Boolean {
            val since = usage.lastActiveAt ?: usage.firstLaunchedAt ?: return false
            return Duration.between(since, now.toInstant()) >= INACTIVITY
        }

        private fun ReminderInput.canRemindInactivity(): Boolean {
            if (force) return true
            val last = lastInactivityReminderAt ?: return true
            return Duration.between(last, now.toInstant()) >= INACTIVITY
        }

        private fun ReminderInput.canRemindStreak(time: LocalTime): Boolean {
            if (force) return true
            return time >= EVENING_START && time < QUIET_START && lastStreakReminderDate != now.toLocalDate()
        }

        private companion object {
            val QUIET_START: LocalTime = LocalTime.of(22, 0)
            val QUIET_END: LocalTime = LocalTime.of(9, 0)
            val EVENING_START: LocalTime = LocalTime.of(17, 0)
            val INACTIVITY: Duration = Duration.ofDays(3)
        }
    }
