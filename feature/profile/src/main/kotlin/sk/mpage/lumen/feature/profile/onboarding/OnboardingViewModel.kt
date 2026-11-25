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
import sk.mpage.lumen.core.domain.repository.UserProfileRepository
import sk.mpage.lumen.core.model.Avatar
import sk.mpage.lumen.core.ui.UiText
import sk.mpage.lumen.feature.profile.R
import sk.mpage.lumen.feature.profile.component.PlayerName
import javax.inject.Inject

@HiltViewModel
internal class OnboardingViewModel
    @Inject
    constructor(
        private val profiles: UserProfileRepository,
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
                    state.update { it.copy(presetKey = action.key) }
                }

                OnboardingAction.Submit -> {
                    submit()
                }
            }
        }

        private fun submit() {
            val current = state.value
            if (current.isSaving) return
            val error = PlayerName.validate(current.name)
            if (error != null) {
                state.update { it.copy(nameError = error) }
                return
            }
            state.update { it.copy(isSaving = true) }
            viewModelScope.launch {
                when (profiles.completeOnboarding(current.name.trim(), Avatar.Preset(current.presetKey))) {
                    is AppResult.Success -> {
                        effectChannel.send(OnboardingEffect.Finished)
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
