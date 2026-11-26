package sk.mpage.lumen.feature.game.play

import kotlinx.collections.immutable.ImmutableList
import sk.mpage.lumen.core.common.result.DataError
import sk.mpage.lumen.core.designsystem.component.AnswerState
import sk.mpage.lumen.core.model.Difficulty
import sk.mpage.lumen.core.model.QuestionType

internal sealed interface GameUiState {
    data object Loading : GameUiState

    data class LoadFailed(
        val error: DataError,
    ) : GameUiState

    data class Playing(
        val position: Int,
        val total: Int?,
        val categoryName: String,
        val difficulty: Difficulty,
        val type: QuestionType,
        val text: String,
        val presentationId: Int,
        val options: ImmutableList<OptionUi>,
        val acceptsAnswers: Boolean,
        val feedback: Feedback?,
    ) : GameUiState

    data object Saving : GameUiState

    data class SaveFailed(
        val error: DataError,
    ) : GameUiState
}

internal data class OptionUi(
    val text: String,
    val state: AnswerState,
)

internal sealed interface Feedback {
    data object Correct : Feedback

    data class Wrong(
        val correctAnswer: String,
    ) : Feedback

    data class TimedOut(
        val correctAnswer: String,
    ) : Feedback
}

internal sealed interface GameAction {
    data class OptionTapped(
        val option: String,
        val presentationId: Int,
    ) : GameAction

    data class QuestionShown(
        val presentationId: Int,
    ) : GameAction

    data class VisibilityChanged(
        val visible: Boolean,
    ) : GameAction

    data object NextTapped : GameAction

    data object RetryTapped : GameAction

    data object QuitConfirmed : GameAction
}
