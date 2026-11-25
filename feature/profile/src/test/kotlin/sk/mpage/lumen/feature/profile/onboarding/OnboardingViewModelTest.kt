package sk.mpage.lumen.feature.profile.onboarding

import app.cash.turbine.test
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import sk.mpage.lumen.core.common.result.DataError
import sk.mpage.lumen.core.model.Avatar
import sk.mpage.lumen.core.model.UserProfile
import sk.mpage.lumen.core.testing.MainDispatcherRule
import sk.mpage.lumen.core.testing.repository.FakeUserProfileRepository
import sk.mpage.lumen.core.ui.UiText
import sk.mpage.lumen.feature.profile.R
import sk.mpage.lumen.feature.profile.component.NameError
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class OnboardingViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val profiles = FakeUserProfileRepository()
    private val viewModel = OnboardingViewModel(profiles)

    @Test
    fun `a fresh form shows no error and cannot be submitted`() {
        val state = viewModel.uiState.value

        assertNull(state.nameError)
        assertFalse(state.canSubmit)
    }

    @Test
    fun `a blank name is rejected`() {
        viewModel.onAction(OnboardingAction.NameChanged("   "))

        assertEquals(NameError.BLANK, viewModel.uiState.value.nameError)
        assertFalse(viewModel.uiState.value.canSubmit)
    }

    @Test
    fun `a 25 character name is rejected and 24 characters are accepted`() {
        viewModel.onAction(OnboardingAction.NameChanged("a".repeat(25)))
        assertEquals(NameError.TOO_LONG, viewModel.uiState.value.nameError)

        viewModel.onAction(OnboardingAction.NameChanged("a".repeat(24)))
        assertNull(viewModel.uiState.value.nameError)
        assertTrue(viewModel.uiState.value.canSubmit)
    }

    @Test
    fun `surrounding spaces and unicode letters do not count against the limit`() {
        viewModel.onAction(OnboardingAction.NameChanged("  Žofia Ľubomíra 李雷  "))

        assertNull(viewModel.uiState.value.nameError)
    }

    @Test
    fun `submitting an invalid name shows the error and saves nothing`() {
        viewModel.onAction(OnboardingAction.Submit)

        assertEquals(NameError.BLANK, viewModel.uiState.value.nameError)
        assertFalse(profiles.current.onboarded)
    }

    @Test
    fun `submit saves the trimmed name and chosen preset, then finishes`() =
        runTest {
            viewModel.onAction(OnboardingAction.NameChanged("  Ada  "))
            viewModel.onAction(OnboardingAction.PresetSelected("owl"))
            assertEquals(Avatar.Preset("owl"), viewModel.uiState.value.avatar)

            viewModel.effects.test {
                viewModel.onAction(OnboardingAction.Submit)

                assertEquals(OnboardingEffect.Finished, awaitItem())
            }
            assertEquals(UserProfile("Ada", Avatar.Preset("owl"), onboarded = true), profiles.current)
        }

    @Test
    fun `a picked photo becomes the avatar that is saved`() =
        runTest {
            viewModel.onAction(OnboardingAction.NameChanged("Ada"))
            viewModel.onAction(OnboardingAction.PhotoPicked("content://media/picker/0/42"))
            assertTrue(viewModel.uiState.value.isImportingPhoto)
            assertFalse(viewModel.uiState.value.canSubmit)

            mainDispatcherRule.dispatcher.scheduler.advanceUntilIdle()
            assertEquals(Avatar.Custom("42"), viewModel.uiState.value.avatar)

            viewModel.effects.test {
                viewModel.onAction(OnboardingAction.Submit)
                assertEquals(OnboardingEffect.Finished, awaitItem())
            }
            assertEquals(UserProfile("Ada", Avatar.Custom("42"), onboarded = true), profiles.current)
        }

    @Test
    fun `a photo that cannot be imported keeps the preset and shows a message`() =
        runTest {
            profiles.failNext(DataError.Storage)

            viewModel.effects.test {
                viewModel.onAction(OnboardingAction.PhotoPicked("content://media/picker/0/42"))

                assertEquals(OnboardingEffect.ShowMessage(UiText.Resource(R.string.profile_photo_failed)), awaitItem())
            }
            assertEquals(Avatar.Preset("fox"), viewModel.uiState.value.avatar)
            assertFalse(viewModel.uiState.value.isImportingPhoto)
        }

    @Test
    fun `a failed save shows a message and allows another try`() =
        runTest {
            profiles.failNext(DataError.Storage)
            viewModel.onAction(OnboardingAction.NameChanged("Ada"))

            viewModel.effects.test {
                viewModel.onAction(OnboardingAction.Submit)

                assertEquals(
                    OnboardingEffect.ShowMessage(UiText.Resource(R.string.onboarding_save_failed)),
                    awaitItem(),
                )
            }
            assertFalse(viewModel.uiState.value.isSaving)
            assertTrue(viewModel.uiState.value.canSubmit)
            assertFalse(profiles.current.onboarded)
        }
}
