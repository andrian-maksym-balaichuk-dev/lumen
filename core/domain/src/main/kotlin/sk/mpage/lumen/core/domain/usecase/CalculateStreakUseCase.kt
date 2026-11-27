package sk.mpage.lumen.core.domain.usecase

import sk.mpage.lumen.core.model.StreakDay
import sk.mpage.lumen.core.model.StreakDayStatus
import sk.mpage.lumen.core.model.StreakState
import java.time.LocalDate
import javax.inject.Inject

public class CalculateStreakUseCase
    @Inject
    constructor() {
        public operator fun invoke(
            qualifyingDaysDesc: List<LocalDate>,
            today: LocalDate,
        ): StreakState {
            val days = qualifyingDaysDesc.filterTo(sortedSetOf()) { it <= today }
            val runs = runLengths(days)
            val latest = days.lastOrNull()
            val current =
                if (latest != null && latest >= today.minusDays(1)) runs.last() else 0
            val playedToday = latest == today
            return StreakState(
                current = current,
                best = runs.maxOrNull() ?: 0,
                playedToday = playedToday,
                atRisk = current > 0 && !playedToday,
                challengeProgress = minOf(current, CHALLENGE_DAYS),
                challengeCompleted = current >= CHALLENGE_DAYS,
                challengesCompleted = runs.sumOf { it / CHALLENGE_DAYS },
                lastSevenDays = lastSevenDays(days, today),
            )
        }

        private fun runLengths(ascending: Set<LocalDate>): List<Int> {
            val runs = mutableListOf<Int>()
            var previous: LocalDate? = null
            for (day in ascending) {
                if (previous != null && day == previous.plusDays(1)) {
                    runs[runs.lastIndex] += 1
                } else {
                    runs += 1
                }
                previous = day
            }
            return runs
        }

        private fun lastSevenDays(
            days: Set<LocalDate>,
            today: LocalDate,
        ): List<StreakDay> =
            (CHALLENGE_DAYS - 1L downTo 0L).map { offset ->
                val date = today.minusDays(offset)
                val status =
                    when {
                        date in days -> StreakDayStatus.QUALIFIED
                        date == today -> StreakDayStatus.TODAY_PENDING
                        else -> StreakDayStatus.MISSED
                    }
                StreakDay(date, status)
            }

        private companion object {
            const val CHALLENGE_DAYS = 7
        }
    }
