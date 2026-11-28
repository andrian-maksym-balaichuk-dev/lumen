package sk.mpage.lumen

import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import sk.mpage.lumen.core.domain.usecase.CalculateStreakUseCase
import sk.mpage.lumen.core.domain.usecase.ObserveStreakUseCase
import sk.mpage.lumen.core.model.Avatar
import sk.mpage.lumen.core.model.UserProfile
import sk.mpage.lumen.core.testing.MainDispatcherRule
import sk.mpage.lumen.core.testing.repository.FakeQuizHistoryRepository
import sk.mpage.lumen.core.testing.repository.FakeUserProfileRepository
import sk.mpage.lumen.core.testing.time.FakeTodayProvider
import java.time.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class MainActivityViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `stays loading until the profile is read`() {
        val viewModel = MainActivityViewModel(FakeUserProfileRepository(), streak())

        assertEquals(StartupUiState.Loading, viewModel.uiState.value)
    }

    @Test
    fun `a player who never finished onboarding needs onboarding`() =
        runTest(mainDispatcherRule.dispatcher) {
            val viewModel = MainActivityViewModel(FakeUserProfileRepository(), streak())

            testScheduler.runCurrent()

            assertEquals(StartupUiState.NeedsOnboarding, viewModel.uiState.value)
        }

    @Test
    fun `an onboarded player starts on play and later profile changes do not move the start`() =
        runTest(mainDispatcherRule.dispatcher) {
            val profiles = FakeUserProfileRepository(UserProfile("Ada", Avatar.Preset("fox"), onboarded = true))
            val viewModel = MainActivityViewModel(profiles, streak())
            testScheduler.runCurrent()

            profiles.emit(UserProfile("Ada", Avatar.None, onboarded = false))
            testScheduler.runCurrent()

            assertEquals(StartupUiState.Ready, viewModel.uiState.value)
        }

    @Test
    fun `a streak that ended yesterday puts the awards badge up`() =
        runTest(mainDispatcherRule.dispatcher) {
            history.emitQualifyingDays(listOf(TODAY.minusDays(1)))
            val viewModel = MainActivityViewModel(FakeUserProfileRepository(), streak())
            backgroundScope.launch { viewModel.streakAtRisk.collect() }
            testScheduler.runCurrent()

            assertTrue(viewModel.streakAtRisk.value)
        }

    @Test
    fun `a notification link opens the session once`() {
        val viewModel = MainActivityViewModel(FakeUserProfileRepository(), streak())

        viewModel.onSessionLink(42)
        assertEquals(42L, viewModel.sessionToOpen.value)

        viewModel.onSessionOpened()
        assertEquals(null, viewModel.sessionToOpen.value)
    }

    private val history = FakeQuizHistoryRepository()

    private fun streak() = ObserveStreakUseCase(history, FakeTodayProvider(TODAY), CalculateStreakUseCase())

    private companion object {
        val TODAY: LocalDate = LocalDate.of(2026, 11, 25)
    }
}
