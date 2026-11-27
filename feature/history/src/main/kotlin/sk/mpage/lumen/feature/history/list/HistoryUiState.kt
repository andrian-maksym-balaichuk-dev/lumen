package sk.mpage.lumen.feature.history.list

import kotlinx.collections.immutable.ImmutableList
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
        val categories: ImmutableList<Category>,
        val items: ImmutableList<HistoryListItem>,
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
    val key: String

    data class DayHeader(
        val date: LocalDate,
        val label: DayLabel,
    ) : HistoryListItem {
        override val key: String get() = "day-$date"
    }

    data class Session(
        val session: QuizSession,
    ) : HistoryListItem {
        override val key: String get() = "session-${session.id}"
    }
}
