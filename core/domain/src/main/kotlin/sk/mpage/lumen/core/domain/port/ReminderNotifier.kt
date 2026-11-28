package sk.mpage.lumen.core.domain.port

import sk.mpage.lumen.core.model.ReminderDecision

public interface ReminderNotifier {
    public fun areNotificationsAllowed(): Boolean

    public fun showReminder(
        decision: ReminderDecision,
        playerName: String,
    ): Boolean
}
