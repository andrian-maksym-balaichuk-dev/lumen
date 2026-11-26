package sk.mpage.lumen.feature.history.list

import sk.mpage.lumen.core.model.Category
import sk.mpage.lumen.core.model.GameMode
import sk.mpage.lumen.core.model.HistoryFilter
import sk.mpage.lumen.core.model.HistorySort
import sk.mpage.lumen.core.model.QuizSession
import java.time.LocalDate

internal sealed interface HistoryUiState {
    data object Loading : HistoryUiState

    data object Empty : HistoryUiState

    data class Content(
        val filter: HistoryFilter,
        val categories: List<Category>,
        val items: List<HistoryListItem>,
    ) : HistoryUiState
}

internal sealed interface HistoryAction {
    data class SortChanged(
        val sort: HistorySort,
    ) : HistoryAction

    data class CategoryChanged(
        val categoryId: Int?,
    ) : HistoryAction

    data class ModeChanged(
        val mode: GameMode?,
    ) : HistoryAction

    data object FiltersCleared : HistoryAction
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
