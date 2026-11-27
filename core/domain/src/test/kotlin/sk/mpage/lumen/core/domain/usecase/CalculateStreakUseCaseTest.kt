package sk.mpage.lumen.core.domain.usecase

import sk.mpage.lumen.core.model.StreakDay
import sk.mpage.lumen.core.model.StreakDayStatus.MISSED
import sk.mpage.lumen.core.model.StreakDayStatus.QUALIFIED
import sk.mpage.lumen.core.model.StreakDayStatus.TODAY_PENDING
import java.time.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class CalculateStreakUseCaseTest {
    private val calculate = CalculateStreakUseCase()
    private val today = LocalDate.of(2026, 11, 25)

    @Test
    fun `no qualifying days gives an empty streak`() {
        val state = calculate(emptyList(), today)

        assertEquals(0, state.current)
        assertEquals(0, state.best)
        assertFalse(state.playedToday)
        assertFalse(state.atRisk)
        assertEquals(0, state.challengesCompleted)
    }

    @Test
    fun `playing today starts a streak that is not at risk`() {
        val state = calculate(listOf(today), today)

        assertEquals(1, state.current)
        assertTrue(state.playedToday)
        assertFalse(state.atRisk)
    }

    @Test
    fun `a streak ending yesterday is at risk`() {
        val state = calculate(listOf(today.minusDays(1)), today)

        assertEquals(1, state.current)
        assertFalse(state.playedToday)
        assertTrue(state.atRisk)
    }

    @Test
    fun `a missed day resets the streak but keeps the best`() {
        val state = calculate(listOf(today.minusDays(2)), today)

        assertEquals(0, state.current)
        assertEquals(1, state.best)
        assertFalse(state.atRisk)
    }

    @Test
    fun `seven days ending today complete the challenge`() {
        val state = calculate(run(endingAt = today, length = 7), today)

        assertEquals(7, state.current)
        assertEquals(7, state.challengeProgress)
        assertTrue(state.challengeCompleted)
        assertEquals(1, state.challengesCompleted)
        assertFalse(state.atRisk)
    }

    @Test
    fun `seven days ending yesterday are completed and at risk`() {
        val state = calculate(run(endingAt = today.minusDays(1), length = 7), today)

        assertEquals(7, state.current)
        assertTrue(state.challengeCompleted)
        assertTrue(state.atRisk)
    }

    @Test
    fun `fourteen days complete two challenges and keep counting`() {
        val state = calculate(run(endingAt = today, length = 14), today)

        assertEquals(14, state.current)
        assertEquals(7, state.challengeProgress)
        assertEquals(2, state.challengesCompleted)
    }

    @Test
    fun `a gap splits runs and the best is the longest`() {
        val days = run(endingAt = today, length = 3) + run(endingAt = today.minusDays(4), length = 5)

        val state = calculate(days, today)

        assertEquals(3, state.current)
        assertEquals(5, state.best)
    }

    @Test
    fun `a duplicate day counts once`() {
        val state = calculate(listOf(today, today, today.minusDays(1)), today)

        assertEquals(2, state.current)
    }

    @Test
    fun `dates after today are ignored`() {
        val state = calculate(listOf(today.plusDays(1), today.minusDays(1)), today)

        assertEquals(1, state.current)
        assertFalse(state.playedToday)
        assertTrue(state.atRisk)
    }

    @Test
    fun `the spring forward week keeps the run unbroken`() {
        val dstToday = LocalDate.of(2026, 3, 31)

        val state = calculate(run(endingAt = dstToday, length = 5), dstToday)

        assertEquals(5, state.current)
    }

    @Test
    fun `runs are counted from unsorted input`() {
        val state = calculate(run(endingAt = today, length = 4).shuffled(kotlin.random.Random(7)), today)

        assertEquals(4, state.current)
    }

    @Test
    fun `the last seven days run oldest first with today pending`() {
        val days = listOf(today.minusDays(1), today.minusDays(2), today.minusDays(5))

        val state = calculate(days, today)

        assertEquals(
            listOf(MISSED, QUALIFIED, MISSED, MISSED, QUALIFIED, QUALIFIED, TODAY_PENDING)
                .mapIndexed { index, status -> StreakDay(today.minusDays(6L - index), status) },
            state.lastSevenDays,
        )
    }

    @Test
    fun `today is qualified once played`() {
        val state = calculate(listOf(today), today)

        assertEquals(StreakDay(today, QUALIFIED), state.lastSevenDays.last())
    }

    private fun run(
        endingAt: LocalDate,
        length: Int,
    ): List<LocalDate> = (0 until length).map { endingAt.minusDays(it.toLong()) }
}
