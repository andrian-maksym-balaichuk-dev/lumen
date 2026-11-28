package sk.mpage.lumen.core.testing.port

import sk.mpage.lumen.core.domain.port.AchievementNotifier
import sk.mpage.lumen.core.domain.port.ReminderNotifier
import sk.mpage.lumen.core.model.ReminderDecision

public class RecordingNotifier(
    public var allowed: Boolean = true,
    public var posts: Boolean = true,
) : ReminderNotifier,
    AchievementNotifier {
    private val shown = mutableListOf<ReminderDecision>()
    private val shownRecords = mutableListOf<Long>()

    public val reminders: List<ReminderDecision> get() = shown

    public val recordSessions: List<Long> get() = shownRecords

    override fun showMarathonRecord(
        sessionId: Long,
        correct: Int,
        previousBest: Int,
    ) {
        shownRecords += sessionId
    }

    override fun areNotificationsAllowed(): Boolean = allowed

    override fun showReminder(
        decision: ReminderDecision,
        playerName: String,
    ): Boolean {
        if (!allowed || !posts) return false
        shown += decision
        return true
    }
}
