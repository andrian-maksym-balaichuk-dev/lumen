package sk.mpage.lumen.feature.history.list

import sk.mpage.lumen.core.model.QuizSession
import java.time.LocalDate

internal sealed interface HistoryUiState {
    data object Loading : HistoryUiState

    data object Empty : HistoryUiState

    data class Content(
        val items: List<HistoryListItem>,
    ) : HistoryUiState
}

internal enum class DayLabel {
    TODAY,
    YESTERDAY,
    DATE,
}

internal sealed interface HistoryListItem {
    data class DayHeader(
        val date: LocalDate,
        val label: DayLabel,
    ) : HistoryListItem

    data class Session(
        val session: QuizSession,
    ) : HistoryListItem
}
