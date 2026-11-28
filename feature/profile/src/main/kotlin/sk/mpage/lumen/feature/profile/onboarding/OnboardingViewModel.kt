package sk.mpage.lumen.feature.profile.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import sk.mpage.lumen.core.common.result.AppResult
import sk.mpage.lumen.core.domain.repository.SettingsRepository
import sk.mpage.lumen.core.domain.repository.UserProfileRepository
import sk.mpage.lumen.core.model.Avatar
import sk.mpage.lumen.core.model.AvatarInput
import sk.mpage.lumen.core.ui.UiText
import sk.mpage.lumen.feature.profile.R
import sk.mpage.lumen.feature.profile.component.PlayerName
import javax.inject.Inject

@HiltViewModel
internal class OnboardingViewModel
    @Inject
    constructor(
        private val profiles: UserProfileRepository,
        private val settings: SettingsRepository,
    ) : ViewModel() {
        private val state = MutableStateFlow(OnboardingUiState())
        val uiState: StateFlow<OnboardingUiState> = state.asStateFlow()

        private val effectChannel = Channel<OnboardingEffect>(Channel.BUFFERED)
        val effects: Flow<OnboardingEffect> = effectChannel.receiveAsFlow()

        fun onAction(action: OnboardingAction) {
            when (action) {
                is OnboardingAction.NameChanged -> {
                    state.update { it.copy(name = action.name, nameError = PlayerName.validate(action.name)) }
                }

                is OnboardingAction.PresetSelected -> {
                    state.update { it.copy(avatar = Avatar.Preset(action.key)) }
                }

                is OnboardingAction.PhotoPicked -> {
                    importPhoto(action.uriString)
                }

                OnboardingAction.Submit -> {
                    submit()
                }

                is OnboardingAction.RemindersAnswered -> {
                    finish(action.allowed)
                }
            }
        }

        private fun importPhoto(uriString: String) {
            if (state.value.isImportingPhoto) return
            state.update { it.copy(isImportingPhoto = true) }
            viewModelScope.launch {
                when (val result = profiles.updateAvatar(AvatarInput.Picked(uriString))) {
                    is AppResult.Success -> {
                        state.update { it.copy(avatar = result.value, isImportingPhoto = false) }
                    }

                    is AppResult.Failure -> {
                        state.update { it.copy(isImportingPhoto = false) }
                        effectChannel.send(OnboardingEffect.ShowMessage(UiText.Resource(R.string.profile_photo_failed)))
                    }
                }
            }
        }

        private fun finish(remindersAllowed: Boolean) {
            viewModelScope.launch {
                if (!remindersAllowed) {
                    settings.setStreakReminders(false)
                    settings.setInactivityReminders(false)
                }
                effectChannel.send(OnboardingEffect.Finished)
            }
        }

        private fun submit() {
            val current = state.value
            if (current.isSaving || current.isImportingPhoto) return
            val error = PlayerName.validate(current.name)
            if (error != null) {
                state.update { it.copy(nameError = error) }
                return
            }
            state.update { it.copy(isSaving = true) }
            viewModelScope.launch {
                when (profiles.completeOnboarding(current.name.trim(), current.avatar)) {
                    is AppResult.Success -> {
                        state.update { it.copy(isSaving = false, step = OnboardingStep.REMINDERS) }
                    }

                    is AppResult.Failure -> {
                        state.update { it.copy(isSaving = false) }
                        effectChannel.send(
                            OnboardingEffect.ShowMessage(UiText.Resource(R.string.onboarding_save_failed)),
                        )
                    }
                }
            }
        }
    }
