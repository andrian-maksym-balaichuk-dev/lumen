package sk.mpage.lumen.feature.profile.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import sk.mpage.lumen.core.common.result.AppResult
import sk.mpage.lumen.core.domain.port.ReminderCheck
import sk.mpage.lumen.core.domain.repository.SettingsRepository
import sk.mpage.lumen.core.domain.repository.UserProfileRepository
import sk.mpage.lumen.core.model.AvatarInput
import sk.mpage.lumen.core.model.ManualCheck
import sk.mpage.lumen.core.ui.UiText
import sk.mpage.lumen.feature.profile.R
import sk.mpage.lumen.feature.profile.component.PlayerName
import javax.inject.Inject

@HiltViewModel
internal class ProfileViewModel
    @Inject
    constructor(
        private val profiles: UserProfileRepository,
        private val settings: SettingsRepository,
        private val reminderCheck: ReminderCheck,
    ) : ViewModel() {
        private val importingPhoto = MutableStateFlow(false)
        private var checkJob: Job? = null

        private val effectChannel = Channel<ProfileEffect>(Channel.BUFFERED)
        val effects: Flow<ProfileEffect> = effectChannel.receiveAsFlow()

        val uiState: StateFlow<ProfileUiState> =
            combine(
                profiles.observeProfile(),
                settings.observeSettings(),
                importingPhoto,
                reminderCheck.status,
            ) { profile, prefs, importing, reminders ->
                ProfileUiState.Content(
                    name = profile.name,
                    avatar = profile.avatar,
                    isImportingPhoto = importing,
                    streakReminders = prefs.streakReminders,
                    inactivityReminders = prefs.inactivityReminders,
                    reminderStatus = reminders,
                )
            }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ProfileUiState.Loading)

        fun onAction(action: ProfileAction) {
            when (action) {
                is ProfileAction.Renamed -> {
                    rename(action.name)
                }

                is ProfileAction.PresetSelected -> {
                    updateAvatar(AvatarInput.Preset(action.key), R.string.profile_avatar_failed)
                }

                is ProfileAction.PhotoPicked -> {
                    importPhoto(action.uriString)
                }

                is ProfileAction.StreakRemindersChanged -> {
                    viewModelScope.launch { settings.setStreakReminders(action.enabled) }
                }

                is ProfileAction.InactivityRemindersChanged -> {
                    viewModelScope.launch { settings.setInactivityReminders(action.enabled) }
                }

                ProfileAction.RunReminderCheck -> {
                    runReminderCheck()
                }
            }
        }

        private fun runReminderCheck() {
            checkJob?.cancel()
            val progress = reminderCheck.runNow()
            checkJob =
                viewModelScope.launch {
                    when (progress.first { it != ManualCheck.QUEUED && it != ManualCheck.RUNNING }) {
                        ManualCheck.POSTED -> showMessage(R.string.profile_reminder_posted)
                        ManualCheck.NOT_ALLOWED -> showMessage(R.string.profile_notifications_off)
                        ManualCheck.NOTHING_TO_REMIND -> showMessage(R.string.profile_reminder_nothing)
                        else -> Unit
                    }
                }
        }

        private fun rename(name: String) {
            if (PlayerName.validate(name) != null) return
            viewModelScope.launch {
                if (profiles.updateName(name.trim()) is AppResult.Failure) showMessage(R.string.profile_rename_failed)
            }
        }

        private fun importPhoto(uriString: String) {
            if (importingPhoto.value) return
            importingPhoto.value = true
            viewModelScope.launch {
                updateAvatarNow(AvatarInput.Picked(uriString), R.string.profile_photo_failed)
                importingPhoto.value = false
            }
        }

        private fun updateAvatar(
            input: AvatarInput,
            failureMessage: Int,
        ) {
            viewModelScope.launch { updateAvatarNow(input, failureMessage) }
        }

        private suspend fun updateAvatarNow(
            input: AvatarInput,
            failureMessage: Int,
        ) {
            if (profiles.updateAvatar(input) is AppResult.Failure) showMessage(failureMessage)
        }

        private suspend fun showMessage(message: Int) {
            effectChannel.send(ProfileEffect.ShowMessage(UiText.Resource(message)))
        }
    }
