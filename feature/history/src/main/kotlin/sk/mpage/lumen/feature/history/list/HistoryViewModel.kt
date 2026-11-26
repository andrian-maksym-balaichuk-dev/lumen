package sk.mpage.lumen.feature.history.list

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import sk.mpage.lumen.core.domain.repository.QuizHistoryRepository
import sk.mpage.lumen.core.model.GameMode
import sk.mpage.lumen.core.model.HistoryFilter
import sk.mpage.lumen.core.model.HistorySort
import sk.mpage.lumen.core.model.QuizSession
import java.time.Clock
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
internal class HistoryViewModel
    @Inject
    constructor(
        history: QuizHistoryRepository,
        private val savedStateHandle: SavedStateHandle,
        private val clock: Clock,
    ) : ViewModel() {
        private val filter = MutableStateFlow(restoreFilter())

        @OptIn(ExperimentalCoroutinesApi::class)
        val uiState: StateFlow<HistoryUiState> =
            filter
                .flatMapLatest { current ->
                    combine(history.observeSessions(current), history.observePlayedCategories()) { sessions, played ->
                        if (sessions.isEmpty() && current == HistoryFilter.Default) {
                            HistoryUiState.Empty
                        } else {
                            HistoryUiState.Content(current, played, items(sessions, current.sort))
                        }
                    }
                }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HistoryUiState.Loading)

        fun onAction(action: HistoryAction) {
            when (action) {
                is HistoryAction.SortChanged -> change { it.copy(sort = action.sort) }
                is HistoryAction.CategoryChanged -> change { it.copy(categoryId = action.categoryId) }
                is HistoryAction.ModeChanged -> change { it.copy(mode = action.mode) }
                HistoryAction.FiltersCleared -> change { HistoryFilter.Default }
            }
        }

        private fun change(transform: (HistoryFilter) -> HistoryFilter) {
            filter.update(transform)
            val current = filter.value
            savedStateHandle[KEY_CATEGORY] = current.categoryId
            savedStateHandle[KEY_MODE] = current.mode?.code
            savedStateHandle[KEY_SORT] = current.sort.name
        }

        private fun restoreFilter(): HistoryFilter =
            HistoryFilter(
                categoryId = savedStateHandle.get<Int>(KEY_CATEGORY),
                mode = savedStateHandle.get<String>(KEY_MODE)?.let(GameMode::fromCode),
                sort =
                    savedStateHandle
                        .get<String>(KEY_SORT)
                        ?.let { name -> HistorySort.entries.firstOrNull { it.name == name } }
                        ?: HistorySort.NEWEST,
            )

        private fun items(
            sessions: List<QuizSession>,
            sort: HistorySort,
        ): List<HistoryListItem> {
            if (sort != HistorySort.NEWEST) return sessions.map(HistoryListItem::Session)
            val today = LocalDate.now(clock)
            return sessions.groupBy { it.localDate }.flatMap { (date, daySessions) ->
                val label =
                    when (date) {
                        today -> DayLabel.TODAY
                        today.minusDays(1) -> DayLabel.YESTERDAY
                        else -> DayLabel.DATE
                    }
                listOf(HistoryListItem.DayHeader(date, label)) + daySessions.map(HistoryListItem::Session)
            }
        }

        private companion object {
            const val KEY_CATEGORY = "history_category"
            const val KEY_MODE = "history_mode"
            const val KEY_SORT = "history_sort"
        }
    }
