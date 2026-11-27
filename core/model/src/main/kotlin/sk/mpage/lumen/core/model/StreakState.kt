package sk.mpage.lumen.core.model

import java.time.LocalDate

public data class StreakState(
    val current: Int,
    val best: Int,
    val playedToday: Boolean,
    val atRisk: Boolean,
    val challengeProgress: Int,
    val challengeCompleted: Boolean,
    val challengesCompleted: Int,
    val lastSevenDays: List<StreakDay>,
)

public data class StreakDay(
    val date: LocalDate,
    val status: StreakDayStatus,
)

public enum class StreakDayStatus { QUALIFIED, MISSED, TODAY_PENDING }
