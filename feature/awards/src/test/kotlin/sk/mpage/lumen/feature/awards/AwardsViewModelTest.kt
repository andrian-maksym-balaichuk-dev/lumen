package sk.mpage.lumen.feature.awards

import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import sk.mpage.lumen.core.domain.usecase.CalculateStreakUseCase
import sk.mpage.lumen.core.domain.usecase.ObserveStreakUseCase
import sk.mpage.lumen.core.testing.MainDispatcherRule
import sk.mpage.lumen.core.testing.repository.FakeQuizHistoryRepository
import sk.mpage.lumen.core.testing.time.FakeTodayProvider
import java.time.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class AwardsViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val today = LocalDate.of(2026, 11, 25)
    private val history = FakeQuizHistoryRepository()

    @Test
    fun `one qualifying game today shows a streak of one and the marathon best`() =
        runTest {
            history.emitQualifyingDays(listOf(today))
            history.emitMarathonBest(24)
            val viewModel = viewModel()
            backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.uiState.collect() }
            advanceUntilIdle()

            val content = assertIs<AwardsUiState.Content>(viewModel.uiState.value)
            assertEquals(1, content.streak.current)
            assertTrue(content.streak.playedToday)
            assertEquals(24, content.marathonBest)
        }

    @Test
    fun `no games shows zero records`() =
        runTest {
            val viewModel = viewModel()
            backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.uiState.collect() }
            advanceUntilIdle()

            val content = assertIs<AwardsUiState.Content>(viewModel.uiState.value)
            assertEquals(0, content.streak.current)
            assertEquals(null, content.marathonBest)
            assertEquals(0, content.gamesPlayed)
        }

    private fun viewModel() =
        AwardsViewModel(ObserveStreakUseCase(history, FakeTodayProvider(today), CalculateStreakUseCase()), history)
}
