package sk.mpage.lumen.feature.profile.settings

import sk.mpage.lumen.core.model.Avatar
import sk.mpage.lumen.core.ui.UiText

internal sealed interface ProfileUiState {
    data object Loading : ProfileUiState

    data class Content(
        val name: String,
        val avatar: Avatar,
        val isImportingPhoto: Boolean = false,
        val streakReminders: Boolean = true,
        val inactivityReminders: Boolean = true,
    ) : ProfileUiState
}

internal sealed interface ProfileAction {
    data class Renamed(
        val name: String,
    ) : ProfileAction

    data class PresetSelected(
        val key: String,
    ) : ProfileAction

    data class PhotoPicked(
        val uriString: String,
    ) : ProfileAction

    data class StreakRemindersChanged(
        val enabled: Boolean,
    ) : ProfileAction

    data class InactivityRemindersChanged(
        val enabled: Boolean,
    ) : ProfileAction
}

internal sealed interface ProfileEffect {
    data class ShowMessage(
        val message: UiText,
    ) : ProfileEffect
}
