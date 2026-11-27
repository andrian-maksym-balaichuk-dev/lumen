package sk.mpage.lumen.feature.lobby

import kotlinx.collections.immutable.ImmutableList
import sk.mpage.lumen.core.common.result.DataError
import sk.mpage.lumen.core.model.Avatar
import sk.mpage.lumen.core.model.Category
import sk.mpage.lumen.core.model.Difficulty
import sk.mpage.lumen.core.model.GameMode
import sk.mpage.lumen.core.model.QuestionType
import sk.mpage.lumen.core.model.QuizConfig
import sk.mpage.lumen.core.model.StreakState
import sk.mpage.lumen.core.ui.UiText
import java.time.LocalTime

internal enum class Greeting {
    MORNING,
    AFTERNOON,
    EVENING,
    NIGHT,
    ;

    companion object {
        fun at(time: LocalTime): Greeting =
            when (time.hour) {
                in 5..11 -> MORNING
                in 12..17 -> AFTERNOON
                in 18..23 -> EVENING
                else -> NIGHT
            }
    }
}

internal sealed interface CategoriesUiState {
    data object Loading : CategoriesUiState

    data class Loaded(
        val categories: ImmutableList<Category>,
    ) : CategoriesUiState

    data class Failed(
        val error: DataError,
    ) : CategoriesUiState
}

internal data class LobbyUiState(
    val greeting: Greeting = Greeting.MORNING,
    val playerName: String = "",
    val avatar: Avatar = Avatar.None,
    val mode: GameMode = GameMode.CLASSIC,
    val difficulty: Difficulty = Difficulty.ANY,
    val type: QuestionType = QuestionType.MULTIPLE_CHOICE,
    val categoryId: Int? = null,
    val categories: CategoriesUiState = CategoriesUiState.Loading,
    val isRefreshing: Boolean = false,
    val isOffline: Boolean = false,
    val streak: StreakState? = null,
    val marathonBest: Int? = null,
)

internal sealed interface LobbyAction {
    data class ModeSelected(
        val mode: GameMode,
    ) : LobbyAction

    data class DifficultySelected(
        val difficulty: Difficulty,
    ) : LobbyAction

    data class TypeSelected(
        val type: QuestionType,
    ) : LobbyAction

    data class CategorySelected(
        val categoryId: Int?,
    ) : LobbyAction

    data object RefreshPulled : LobbyAction

    data object RetryTapped : LobbyAction

    data object StartTapped : LobbyAction
}

internal sealed interface LobbyEffect {
    data class StartGame(
        val config: QuizConfig,
    ) : LobbyEffect

    data class ShowMessage(
        val message: UiText,
    ) : LobbyEffect
}
