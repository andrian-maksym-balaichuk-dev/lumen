package sk.mpage.lumen.core.domain.port

public interface AchievementNotifier {
    public fun showMarathonRecord(
        sessionId: Long,
        correct: Int,
        previousBest: Int,
    )
}
