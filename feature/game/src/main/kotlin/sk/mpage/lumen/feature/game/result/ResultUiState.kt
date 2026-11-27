package sk.mpage.lumen.feature.game.result

import sk.mpage.lumen.core.model.Avatar
import sk.mpage.lumen.core.model.QuizConfig

internal sealed interface ResultUiState {
    data object Loading : ResultUiState

    data object NotFound : ResultUiState

    data class Content(
        val playerName: String,
        val avatar: Avatar,
        val config: QuizConfig,
        val answeredEveryQuestion: Boolean,
        val correct: Int,
        val total: Int,
        val wrong: Int,
        val timeouts: Int,
        val percent: Int,
        val durationSeconds: Long,
        val averageAnswerMs: Long,
        val isNewBest: Boolean = false,
        val previousBest: Int? = null,
    ) : ResultUiState
}
