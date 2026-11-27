package sk.mpage.lumen.feature.history.list

import androidx.lifecycle.SavedStateHandle
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import sk.mpage.lumen.core.model.Difficulty
import sk.mpage.lumen.core.model.EndReason
import sk.mpage.lumen.core.model.GameMode
import sk.mpage.lumen.core.model.HistoryFilter
import sk.mpage.lumen.core.model.HistorySort
import sk.mpage.lumen.core.model.QuestionType
import sk.mpage.lumen.core.model.QuizConfig
import sk.mpage.lumen.core.model.QuizSession
import sk.mpage.lumen.core.testing.MainDispatcherRule
import sk.mpage.lumen.core.testing.repository.FakeQuizHistoryRepository
import sk.mpage.lumen.core.testing.time.FakeTodayProvider
import java.time.Instant
import java.time.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class HistoryViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val history = FakeQuizHistoryRepository()
    private val todayProvider = FakeTodayProvider(LocalDate.of(2026, 11, 26))

    @Test
    fun `no games shows the empty state`() =
        runTest {
            val viewModel = HistoryViewModel(history, SavedStateHandle(), todayProvider)
            backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.uiState.collect() }
            advanceUntilIdle()

            assertEquals(HistoryUiState.Empty, viewModel.uiState.value)
        }

    @Test
    fun `sessions are grouped under today, yesterday and older dates`() =
        runTest {
            val today = session(3, LocalDate.of(2026, 11, 26))
            val yesterday = session(2, LocalDate.of(2026, 11, 25))
            val older = session(1, LocalDate.of(2026, 11, 20))
            history.emitSessions(listOf(today, yesterday, older))
            val viewModel = HistoryViewModel(history, SavedStateHandle(), todayProvider)
            backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.uiState.collect() }
            advanceUntilIdle()

            assertEquals(
                HistoryUiState.Content(
                    HistoryFilter.Default,
                    persistentListOf(),
                    persistentListOf(
                        HistoryListItem.DayHeader(today.localDate, DayLabel.TODAY),
                        HistoryListItem.Session(today),
                        HistoryListItem.DayHeader(yesterday.localDate, DayLabel.YESTERDAY),
                        HistoryListItem.Session(yesterday),
                        HistoryListItem.DayHeader(older.localDate, DayLabel.DATE),
                        HistoryListItem.Session(older),
                    ),
                ),
                viewModel.uiState.value,
            )
        }

    @Test
    fun `day headers move on when the date changes`() =
        runTest {
            val newest = session(1, LocalDate.of(2026, 11, 26))
            history.emitSessions(listOf(newest))
            val viewModel = HistoryViewModel(history, SavedStateHandle(), todayProvider)
            backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.uiState.collect() }
            advanceUntilIdle()

            todayProvider.setToday(LocalDate.of(2026, 11, 27))
            advanceUntilIdle()

            val content = assertIs<HistoryUiState.Content>(viewModel.uiState.value)
            assertEquals(HistoryListItem.DayHeader(newest.localDate, DayLabel.YESTERDAY), content.items.first())
        }

    @Test
    fun `changing a filter queries again and other sorts have no day headers`() =
        runTest {
            val classic = session(2, LocalDate.of(2026, 11, 26))
            val marathon = session(1, LocalDate.of(2026, 11, 26), GameMode.MARATHON)
            history.emitSessions(listOf(classic, marathon))
            val viewModel = HistoryViewModel(history, SavedStateHandle(), todayProvider)
            backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.uiState.collect() }

            viewModel.onAction(HistoryAction.SortChanged(HistorySort.BEST_SCORE))
            viewModel.onAction(HistoryAction.ModeChanged(GameMode.MARATHON))
            advanceUntilIdle()

            val content = assertIs<HistoryUiState.Content>(viewModel.uiState.value)
            assertEquals<List<HistoryListItem>>(listOf(HistoryListItem.Session(marathon)), content.items)
            assertEquals(
                HistoryFilter(null, GameMode.MARATHON, HistorySort.BEST_SCORE),
                history.requestedFilters.last(),
            )
        }

    @Test
    fun `a new sort order is emitted with the content`() =
        runTest {
            history.emitSessions(listOf(session(1, LocalDate.of(2026, 11, 26))))
            val viewModel = HistoryViewModel(history, SavedStateHandle(), todayProvider)
            backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.uiState.collect() }
            advanceUntilIdle()
            assertEquals(HistorySort.NEWEST, assertIs<HistoryUiState.Content>(viewModel.uiState.value).filter.sort)

            viewModel.onAction(HistoryAction.SortChanged(HistorySort.FASTEST))
            advanceUntilIdle()

            assertEquals(HistorySort.FASTEST, assertIs<HistoryUiState.Content>(viewModel.uiState.value).filter.sort)
        }

    @Test
    fun `every list item has its own stable key`() =
        runTest {
            history.emitSessions(
                listOf(
                    session(3, LocalDate.of(2026, 11, 26)),
                    session(2, LocalDate.of(2026, 11, 26)),
                    session(1, LocalDate.of(2026, 11, 25)),
                ),
            )
            val viewModel = HistoryViewModel(history, SavedStateHandle(), todayProvider)
            backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.uiState.collect() }
            advanceUntilIdle()

            val keys = assertIs<HistoryUiState.Content>(viewModel.uiState.value).items.map { it.key }
            assertEquals(listOf("day-2026-11-26", "session-3", "session-2", "day-2026-11-25", "session-1"), keys)
        }

    @Test
    fun `a filter with no matches is not the empty history`() =
        runTest {
            history.emitSessions(listOf(session(1, LocalDate.of(2026, 11, 26))))
            val viewModel = HistoryViewModel(history, SavedStateHandle(), todayProvider)
            backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.uiState.collect() }

            viewModel.onAction(HistoryAction.ModeChanged(GameMode.MARATHON))
            advanceUntilIdle()
            assertEquals(0, assertIs<HistoryUiState.Content>(viewModel.uiState.value).items.size)

            viewModel.onAction(HistoryAction.FiltersCleared)
            advanceUntilIdle()
            assertEquals(
                1,
                assertIs<HistoryUiState.Content>(viewModel.uiState.value).items.count { it is HistoryListItem.Session },
            )
        }

    @Test
    fun `the filter survives a new view model with the same saved state`() =
        runTest {
            val handle = SavedStateHandle()
            HistoryViewModel(history, handle, todayProvider).onAction(HistoryAction.SortChanged(HistorySort.FASTEST))
            HistoryViewModel(history, handle, todayProvider).onAction(HistoryAction.CategoryChanged(11))

            val restored = HistoryViewModel(history, handle, todayProvider)
            backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { restored.uiState.collect() }
            advanceUntilIdle()

            assertEquals(HistoryFilter(11, null, HistorySort.FASTEST), history.requestedFilters.last())
        }

    private fun session(
        id: Long,
        date: LocalDate,
        mode: GameMode = GameMode.CLASSIC,
    ) = QuizSession(
        id = id,
        config = QuizConfig(mode, null, "", Difficulty.ANY, QuestionType.MULTIPLE_CHOICE),
        startedAt = Instant.EPOCH,
        finishedAt = Instant.EPOCH,
        localDate = date,
        durationSeconds = 60,
        answerTimeMs = 30_000,
        totalQuestions = 10,
        correctCount = 8,
        wrongCount = 2,
        timeoutCount = 0,
        scorePercent = 80,
        averageAnswerMs = 3_000,
        endReason = EndReason.COMPLETED,
    )
}
