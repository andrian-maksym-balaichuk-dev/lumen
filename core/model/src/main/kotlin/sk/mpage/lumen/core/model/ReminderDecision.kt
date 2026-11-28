package sk.mpage.lumen.core.model

public sealed interface ReminderDecision {
    public data object None : ReminderDecision

    public data object Inactivity : ReminderDecision

    public data class StreakAtRisk(
        val current: Int,
    ) : ReminderDecision

    public data object StartChallenge : ReminderDecision
}
