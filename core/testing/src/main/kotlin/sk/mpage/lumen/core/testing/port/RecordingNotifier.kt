package sk.mpage.lumen.core.testing.port

import sk.mpage.lumen.core.domain.port.ReminderNotifier
import sk.mpage.lumen.core.model.ReminderDecision

public class RecordingNotifier(
    public var allowed: Boolean = true,
    public var posts: Boolean = true,
) : ReminderNotifier {
    private val shown = mutableListOf<ReminderDecision>()

    public val reminders: List<ReminderDecision> get() = shown

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
