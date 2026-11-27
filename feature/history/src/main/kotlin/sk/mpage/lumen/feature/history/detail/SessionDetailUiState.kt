package sk.mpage.lumen.feature.history.detail

import kotlinx.collections.immutable.ImmutableList
import sk.mpage.lumen.core.model.QuestionAttempt
import sk.mpage.lumen.core.model.QuizSession

internal sealed interface SessionDetailUiState {
    data object Loading : SessionDetailUiState

    data object NotFound : SessionDetailUiState

    data class Content(
        val session: QuizSession,
        val attempts: ImmutableList<QuestionAttempt>,
    ) : SessionDetailUiState
}
