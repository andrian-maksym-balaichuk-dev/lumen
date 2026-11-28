package sk.mpage.lumen.feature.profile.settings

import app.cash.turbine.test
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import sk.mpage.lumen.core.common.result.DataError
import sk.mpage.lumen.core.model.Avatar
import sk.mpage.lumen.core.model.ManualCheck
import sk.mpage.lumen.core.model.ReminderStatus
import sk.mpage.lumen.core.model.UserProfile
import sk.mpage.lumen.core.testing.MainDispatcherRule
import sk.mpage.lumen.core.testing.port.FakeReminderCheck
import sk.mpage.lumen.core.testing.repository.FakeSettingsRepository
import sk.mpage.lumen.core.testing.repository.FakeUserProfileRepository
import sk.mpage.lumen.core.ui.UiText
import sk.mpage.lumen.feature.profile.R
import kotlin.test.Test
import kotlin.test.assertEquals

class ProfileViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val profiles = FakeUserProfileRepository(UserProfile("Ada", Avatar.Preset("fox"), onboarded = true))
    private val settings = FakeSettingsRepository()
    private val reminderCheck = FakeReminderCheck()
    private val viewModel by lazy { ProfileViewModel(profiles, settings, reminderCheck) }

    @Test
    fun `shows the stored profile`() =
        runTest {
            viewModel.uiState.test {
                assertEquals(ProfileUiState.Loading, awaitItem())
                assertEquals(ProfileUiState.Content("Ada", Avatar.Preset("fox")), awaitItem())
            }
        }

    @Test
    fun `run now calls the reminder check and reports the result`() =
        runTest {
            viewModel.effects.test {
                viewModel.onAction(ProfileAction.RunReminderCheck)
                assertEquals(1, reminderCheck.runs)

                reminderCheck.finishRun(ManualCheck.RUNNING)
                reminderCheck.finishRun(ManualCheck.NOTHING_TO_REMIND)

                assertEquals(
                    ProfileEffect.ShowMessage(UiText.Resource(R.string.profile_reminder_nothing)),
                    awaitItem(),
                )
            }
        }

    @Test
    fun `the reminder check status is shown`() =
        runTest {
            reminderCheck.emit(ReminderStatus(manualCheck = ManualCheck.RUNNING))

            viewModel.uiState.test {
                skipItems(1)
                assertEquals(ManualCheck.RUNNING, (awaitItem() as ProfileUiState.Content).reminderStatus.manualCheck)
            }
        }

    @Test
    fun `reminder switches are saved`() =
        runTest {
            viewModel.uiState.test {
                skipItems(2)

                viewModel.onAction(ProfileAction.StreakRemindersChanged(false))
                assertEquals(false, (awaitItem() as ProfileUiState.Content).streakReminders)

                viewModel.onAction(ProfileAction.InactivityRemindersChanged(false))
                assertEquals(false, (awaitItem() as ProfileUiState.Content).inactivityReminders)
            }
            assertEquals(false, settings.current.streakReminders)
            assertEquals(false, settings.current.inactivityReminders)
        }

    @Test
    fun `renaming saves the trimmed name and the header follows`() =
        runTest {
            viewModel.uiState.test {
                skipItems(2)

                viewModel.onAction(ProfileAction.Renamed("  Grace  "))

                assertEquals(ProfileUiState.Content("Grace", Avatar.Preset("fox")), awaitItem())
            }
            assertEquals("Grace", profiles.current.name)
        }

    @Test
    fun `an invalid name is ignored`() =
        runTest {
            viewModel.onAction(ProfileAction.Renamed("   "))
            viewModel.onAction(ProfileAction.Renamed("a".repeat(25)))
            mainDispatcherRule.dispatcher.scheduler.advanceUntilIdle()

            assertEquals("Ada", profiles.current.name)
        }

    @Test
    fun `choosing a preset or a photo updates the avatar`() =
        runTest {
            viewModel.onAction(ProfileAction.PresetSelected("panda"))
            mainDispatcherRule.dispatcher.scheduler.advanceUntilIdle()
            assertEquals(Avatar.Preset("panda"), profiles.current.avatar)

            viewModel.onAction(ProfileAction.PhotoPicked("content://media/picker/0/7"))
            mainDispatcherRule.dispatcher.scheduler.advanceUntilIdle()
            assertEquals(Avatar.Custom("7"), profiles.current.avatar)
        }

    @Test
    fun `failures keep the old values and show a message`() =
        runTest {
            viewModel.effects.test {
                profiles.failNext(DataError.Storage)
                viewModel.onAction(ProfileAction.Renamed("Grace"))
                assertEquals(ProfileEffect.ShowMessage(UiText.Resource(R.string.profile_rename_failed)), awaitItem())

                profiles.failNext(DataError.Storage)
                viewModel.onAction(ProfileAction.PhotoPicked("content://media/picker/0/7"))
                assertEquals(ProfileEffect.ShowMessage(UiText.Resource(R.string.profile_photo_failed)), awaitItem())
            }
            assertEquals(UserProfile("Ada", Avatar.Preset("fox"), onboarded = true), profiles.current)
        }
}
