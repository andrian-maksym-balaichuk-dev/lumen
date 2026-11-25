package sk.mpage.lumen.feature.lobby

import sk.mpage.lumen.core.model.Avatar
import sk.mpage.lumen.core.model.Difficulty
import sk.mpage.lumen.core.model.GameMode
import sk.mpage.lumen.core.model.QuestionType
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

internal data class LobbyUiState(
    val greeting: Greeting = Greeting.MORNING,
    val playerName: String = "",
    val avatar: Avatar = Avatar.None,
    val mode: GameMode = GameMode.CLASSIC,
    val difficulty: Difficulty = Difficulty.ANY,
    val type: QuestionType = QuestionType.MULTIPLE_CHOICE,
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
}
