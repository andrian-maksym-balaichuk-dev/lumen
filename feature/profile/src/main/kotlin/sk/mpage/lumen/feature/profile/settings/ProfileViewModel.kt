package sk.mpage.lumen.feature.profile.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import sk.mpage.lumen.core.common.result.AppResult
import sk.mpage.lumen.core.domain.repository.UserProfileRepository
import sk.mpage.lumen.core.model.AvatarInput
import sk.mpage.lumen.core.ui.UiText
import sk.mpage.lumen.feature.profile.R
import sk.mpage.lumen.feature.profile.component.PlayerName
import javax.inject.Inject

@HiltViewModel
internal class ProfileViewModel
    @Inject
    constructor(
        private val profiles: UserProfileRepository,
    ) : ViewModel() {
        private val importingPhoto = MutableStateFlow(false)

        val uiState: StateFlow<ProfileUiState> =
            combine(profiles.observeProfile(), importingPhoto) { profile, importing ->
                ProfileUiState.Content(name = profile.name, avatar = profile.avatar, isImportingPhoto = importing)
            }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ProfileUiState.Loading)

        private val effectChannel = Channel<ProfileEffect>(Channel.BUFFERED)
        val effects: Flow<ProfileEffect> = effectChannel.receiveAsFlow()

        fun onAction(action: ProfileAction) {
            when (action) {
                is ProfileAction.Renamed -> {
                    rename(action.name)
                }

                is ProfileAction.PresetSelected -> {
                    updateAvatar(
                        AvatarInput.Preset(action.key),
                        R.string.profile_avatar_failed,
                    )
                }

                is ProfileAction.PhotoPicked -> {
                    importPhoto(action.uriString)
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
