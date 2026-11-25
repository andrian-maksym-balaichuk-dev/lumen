package sk.mpage.lumen.feature.profile.onboarding

import sk.mpage.lumen.core.designsystem.icon.LumoraAvatars
import sk.mpage.lumen.core.model.Avatar
import sk.mpage.lumen.core.ui.UiText
import sk.mpage.lumen.feature.profile.component.NameError
import sk.mpage.lumen.feature.profile.component.PlayerName

internal data class OnboardingUiState(
    val name: String = "",
    val nameError: NameError? = null,
    val avatar: Avatar = Avatar.Preset(LumoraAvatars.Default.key),
    val isImportingPhoto: Boolean = false,
    val isSaving: Boolean = false,
) {
    val canSubmit: Boolean get() = !isSaving && !isImportingPhoto && PlayerName.validate(name) == null
}

internal sealed interface OnboardingAction {
    data class NameChanged(
        val name: String,
    ) : OnboardingAction

    data class PresetSelected(
        val key: String,
    ) : OnboardingAction

    data class PhotoPicked(
        val uriString: String,
    ) : OnboardingAction

    data object Submit : OnboardingAction
}

internal sealed interface OnboardingEffect {
    data object Finished : OnboardingEffect

    data class ShowMessage(
        val message: UiText,
    ) : OnboardingEffect
}
