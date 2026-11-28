package sk.mpage.lumen.core.model

public data class ShareSummary(
    val mode: GameMode,
    val correct: Int,
    val total: Int,
    val percent: Int,
    val categoryName: String,
    val difficulty: Difficulty,
    val durationSeconds: Long,
    val isNewBest: Boolean = false,
    val streakDays: Int? = null,
)
