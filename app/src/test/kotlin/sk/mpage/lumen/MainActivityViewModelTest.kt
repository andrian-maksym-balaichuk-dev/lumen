package sk.mpage.lumen

import kotlinx.coroutines.test.runTest
import org.junit.Rule
import sk.mpage.lumen.core.model.Avatar
import sk.mpage.lumen.core.model.UserProfile
import sk.mpage.lumen.core.testing.MainDispatcherRule
import sk.mpage.lumen.core.testing.repository.FakeUserProfileRepository
import kotlin.test.Test
import kotlin.test.assertEquals

class MainActivityViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `stays loading until the profile is read`() {
        val viewModel = MainActivityViewModel(FakeUserProfileRepository())

        assertEquals(StartupUiState.Loading, viewModel.uiState.value)
    }

    @Test
    fun `a player who never finished onboarding needs onboarding`() =
        runTest(mainDispatcherRule.dispatcher) {
            val viewModel = MainActivityViewModel(FakeUserProfileRepository())

            testScheduler.runCurrent()

            assertEquals(StartupUiState.NeedsOnboarding, viewModel.uiState.value)
        }

    @Test
    fun `an onboarded player starts on play and later profile changes do not move the start`() =
        runTest(mainDispatcherRule.dispatcher) {
            val profiles = FakeUserProfileRepository(UserProfile("Ada", Avatar.Preset("fox"), onboarded = true))
            val viewModel = MainActivityViewModel(profiles)
            testScheduler.runCurrent()

            profiles.emit(UserProfile("Ada", Avatar.None, onboarded = false))
            testScheduler.runCurrent()

            assertEquals(StartupUiState.Ready, viewModel.uiState.value)
        }
}
