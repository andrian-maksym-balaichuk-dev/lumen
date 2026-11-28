package sk.mpage.lumen.core.domain.usecase

import sk.mpage.lumen.core.model.AppSettings
import sk.mpage.lumen.core.model.AppUsage
import sk.mpage.lumen.core.model.ReminderDecision
import sk.mpage.lumen.core.model.StreakState
import java.time.Duration
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime
import kotlin.test.Test
import kotlin.test.assertEquals

class DecideReminderUseCaseTest {
    private val decide = DecideReminderUseCase()
    private val zone = ZoneId.of("Europe/Bratislava")
    private val today = LocalDate.of(2026, 11, 25)

    @Test
    fun `no notification permission means no reminder`() {
        assertEquals(ReminderDecision.None, decide(input(at(18, 0), allowed = false)))
        assertEquals(ReminderDecision.None, decide(input(at(18, 0), allowed = false, force = true)))
    }

    @Test
    fun `quiet hours start at 22 00 and end at 09 00`() {
        assertEquals(ReminderDecision.None, decide(input(at(22, 0), usage = inactiveFor(5))))
        assertEquals(ReminderDecision.None, decide(input(at(8, 59), usage = inactiveFor(5))))
        assertEquals(ReminderDecision.Inactivity, decide(input(at(9, 0), usage = inactiveFor(5))))
        assertEquals(ReminderDecision.StreakAtRisk(3), decide(input(at(21, 59), streak = streak(3))))
    }

    @Test
    fun `three days away gives a come-back reminder`() {
        assertEquals(ReminderDecision.Inactivity, decide(input(at(12, 0), usage = inactiveFor(3))))
        assertEquals(ReminderDecision.None, decide(input(at(12, 0), usage = inactiveFor(2))))
    }

    @Test
    fun `without any activity the first launch counts`() {
        val usage = AppUsage(lastActiveAt = null, firstLaunchedAt = at(12, 0).minusDays(4).toInstant())

        assertEquals(ReminderDecision.Inactivity, decide(input(at(12, 0), usage = usage)))
        assertEquals(ReminderDecision.None, decide(input(at(12, 0), usage = AppUsage())))
    }

    @Test
    fun `a come-back reminder is sent at most once every three days`() {
        val now = at(12, 0)

        val recent = input(now, usage = inactiveFor(5), lastInactivity = now.minusDays(1).toInstant())
        val old = input(now, usage = inactiveFor(5), lastInactivity = now.minusDays(3).toInstant())

        assertEquals(ReminderDecision.None, decide(recent))
        assertEquals(ReminderDecision.Inactivity, decide(old))
    }

    @Test
    fun `come-back reminders can be switched off`() {
        val settings = AppSettings(inactivityReminders = false)

        assertEquals(ReminderDecision.None, decide(input(at(12, 0), settings = settings, usage = inactiveFor(5))))
    }

    @Test
    fun `the evening window is 17 00 to 22 00`() {
        assertEquals(ReminderDecision.None, decide(input(at(16, 59), streak = streak(3))))
        assertEquals(ReminderDecision.StreakAtRisk(3), decide(input(at(17, 0), streak = streak(3))))
    }

    @Test
    fun `no streak yet asks to start the challenge`() {
        assertEquals(ReminderDecision.StartChallenge, decide(input(at(18, 0), streak = streak(0))))
    }

    @Test
    fun `nothing to remind after a qualifying game today`() {
        assertEquals(ReminderDecision.None, decide(input(at(18, 0), streak = streak(3, playedToday = true))))
    }

    @Test
    fun `the streak reminder is sent once a day`() {
        assertEquals(ReminderDecision.None, decide(input(at(19, 0), streak = streak(3), lastStreak = today)))
        assertEquals(
            ReminderDecision.StreakAtRisk(3),
            decide(input(at(19, 0), streak = streak(3), lastStreak = today.minusDays(1))),
        )
    }

    @Test
    fun `streak reminders can be switched off`() {
        val settings = AppSettings(streakReminders = false)

        assertEquals(ReminderDecision.None, decide(input(at(18, 0), settings = settings, streak = streak(3))))
    }

    @Test
    fun `force skips quiet hours, the evening window and the dedupe`() {
        assertEquals(ReminderDecision.StreakAtRisk(3), decide(input(at(3, 0), streak = streak(3), force = true)))
        assertEquals(ReminderDecision.StartChallenge, decide(input(at(11, 0), streak = streak(0), force = true)))
        assertEquals(
            ReminderDecision.StreakAtRisk(3),
            decide(input(at(19, 0), streak = streak(3), lastStreak = today, force = true)),
        )
        assertEquals(
            ReminderDecision.Inactivity,
            decide(
                input(
                    at(23, 0),
                    usage = inactiveFor(5),
                    lastInactivity = at(23, 0).minusDays(1).toInstant(),
                    force = true,
                ),
            ),
        )
    }

    @Test
    fun `force still respects a qualifying game and the toggles`() {
        assertEquals(
            ReminderDecision.None,
            decide(input(at(11, 0), streak = streak(3, playedToday = true), force = true)),
        )
        assertEquals(
            ReminderDecision.None,
            decide(input(at(11, 0), settings = AppSettings(streakReminders = false), streak = streak(3), force = true)),
        )
    }

    private fun at(
        hour: Int,
        minute: Int,
    ): ZonedDateTime = ZonedDateTime.of(today, LocalTime.of(hour, minute), zone)

    private fun inactiveFor(days: Long) = AppUsage(lastActiveAt = at(12, 0).minus(Duration.ofDays(days)).toInstant())

    private fun streak(
        current: Int,
        playedToday: Boolean = false,
    ) = StreakState(
        current = current,
        best = current,
        playedToday = playedToday,
        atRisk = current > 0 && !playedToday,
        challengeProgress = minOf(current, 7),
        challengeCompleted = current >= 7,
        challengesCompleted = current / 7,
        lastSevenDays = emptyList(),
    )

    private fun input(
        now: ZonedDateTime,
        settings: AppSettings = AppSettings(),
        streak: StreakState = streak(0, playedToday = true),
        usage: AppUsage = AppUsage(lastActiveAt = now.toInstant()),
        lastStreak: LocalDate? = null,
        lastInactivity: java.time.Instant? = null,
        allowed: Boolean = true,
        force: Boolean = false,
    ) = ReminderInput(now, settings, streak, usage, lastStreak, lastInactivity, allowed, force)
}
