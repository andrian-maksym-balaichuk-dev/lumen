package sk.mpage.lumen.feature.awards

import sk.mpage.lumen.core.model.StreakState

internal sealed interface AwardsUiState {
    data object Loading : AwardsUiState

    data class Content(
        val streak: StreakState,
        val marathonBest: Int?,
        val gamesPlayed: Int,
    ) : AwardsUiState
}
