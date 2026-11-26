package sk.mpage.lumen.feature.history.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import sk.mpage.lumen.core.domain.repository.QuizHistoryRepository
import sk.mpage.lumen.core.model.HistoryFilter
import sk.mpage.lumen.core.model.QuizSession
import java.time.Clock
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
internal class HistoryViewModel
    @Inject
    constructor(
        history: QuizHistoryRepository,
        private val clock: Clock,
    ) : ViewModel() {
        val uiState: StateFlow<HistoryUiState> =
            history
                .observeSessions(HistoryFilter.Default)
                .map { sessions ->
                    if (sessions.isEmpty()) HistoryUiState.Empty else HistoryUiState.Content(withDayHeaders(sessions))
                }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HistoryUiState.Loading)

        private fun withDayHeaders(sessions: List<QuizSession>): List<HistoryListItem> {
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
    }
