package sk.mpage.lumen.feature.lobby

import app.cash.turbine.test
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import sk.mpage.lumen.core.common.result.DataError
import sk.mpage.lumen.core.model.Avatar
import sk.mpage.lumen.core.model.Category
import sk.mpage.lumen.core.model.Difficulty
import sk.mpage.lumen.core.model.GameMode
import sk.mpage.lumen.core.model.QuestionType
import sk.mpage.lumen.core.model.UserProfile
import sk.mpage.lumen.core.testing.MainDispatcherRule
import sk.mpage.lumen.core.testing.repository.FakeCategoryRepository
import sk.mpage.lumen.core.testing.repository.FakeUserProfileRepository
import sk.mpage.lumen.core.ui.toUiText
import java.time.Clock
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneOffset
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class LobbyViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val profiles = FakeUserProfileRepository(UserProfile("Ada", Avatar.Preset("owl"), onboarded = true))

    private var categories = FakeCategoryRepository(cached = listOf(GENERAL, BOOKS))

    private fun viewModel(at: LocalTime = LocalTime.NOON): LobbyViewModel {
        val instant = LocalDateTime.of(2026, 11, 25, at.hour, at.minute).toInstant(ZoneOffset.UTC)
        return LobbyViewModel(profiles, categories, Clock.fixed(instant, ZoneOffset.UTC))
    }

    private fun TestScope.started(viewModel: LobbyViewModel): LobbyViewModel {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.uiState.collect() }
        advanceUntilIdle()
        return viewModel
    }

    @Test
    fun `the greeting follows the time of day`() {
        assertEquals(Greeting.NIGHT, Greeting.at(LocalTime.of(0, 0)))
        assertEquals(Greeting.NIGHT, Greeting.at(LocalTime.of(4, 59)))
        assertEquals(Greeting.MORNING, Greeting.at(LocalTime.of(5, 0)))
        assertEquals(Greeting.MORNING, Greeting.at(LocalTime.of(11, 59)))
        assertEquals(Greeting.AFTERNOON, Greeting.at(LocalTime.of(12, 0)))
        assertEquals(Greeting.AFTERNOON, Greeting.at(LocalTime.of(17, 59)))
        assertEquals(Greeting.EVENING, Greeting.at(LocalTime.of(18, 0)))
        assertEquals(Greeting.EVENING, Greeting.at(LocalTime.of(21, 59)))
        assertEquals(Greeting.EVENING, Greeting.at(LocalTime.of(23, 59)))
        assertEquals(Greeting.NIGHT, viewModel(at = LocalTime.of(2, 30)).uiState.value.greeting)
    }

    @Test
    fun `the lobby shows the profile and follows its changes`() =
        runTest {
            viewModel().uiState.test {
                skipItems(1)
                assertEquals("Ada", awaitItem().playerName)

                profiles.emit(UserProfile("Bea", Avatar.Preset("fox"), onboarded = true))

                val renamed = awaitItem()
                assertEquals("Bea", renamed.playerName)
                assertEquals(Avatar.Preset("fox"), renamed.avatar)
            }
        }

    @Test
    fun `a fresh lobby starts with classic, any difficulty and multiple choice`() {
        val state = viewModel().uiState.value

        assertEquals(GameMode.CLASSIC, state.mode)
        assertEquals(Difficulty.ANY, state.difficulty)
        assertEquals(QuestionType.MULTIPLE_CHOICE, state.type)
    }

    @Test
    fun `selections update the state`() =
        runTest {
            val viewModel = viewModel()
            viewModel.uiState.test {
                skipItems(2)

                viewModel.onAction(LobbyAction.DifficultySelected(Difficulty.HARD))
                assertEquals(Difficulty.HARD, awaitItem().difficulty)

                viewModel.onAction(LobbyAction.TypeSelected(QuestionType.TRUE_FALSE))
                val state = awaitItem()
                assertEquals(QuestionType.TRUE_FALSE, state.type)
                assertEquals(Difficulty.HARD, state.difficulty)
                assertEquals("Ada", state.playerName)
            }
        }

    @Test
    fun `opening the lobby refreshes stale categories and lists them`() =
        runTest {
            val viewModel = started(viewModel())

            assertEquals(1, categories.refreshIfStaleCalls)
            assertEquals(0, categories.refreshCalls)
            assertEquals(CategoriesUiState.Loaded(persistentListOf(GENERAL, BOOKS)), viewModel.uiState.value.categories)
        }

    @Test
    fun `a failure with an empty cache shows the error and retry loads the list`() =
        runTest {
            categories = FakeCategoryRepository(remote = listOf(GENERAL))
            categories.failNext(DataError.NoInternet)
            val viewModel = started(viewModel())

            assertEquals(CategoriesUiState.Failed(DataError.NoInternet), viewModel.uiState.value.categories)
            viewModel.effects.test { expectNoEvents() }

            categories.gate = CompletableDeferred()
            viewModel.onAction(LobbyAction.RetryTapped)
            advanceUntilIdle()
            assertEquals(CategoriesUiState.Loading, viewModel.uiState.value.categories)
            assertFalse(viewModel.uiState.value.isRefreshing)

            categories.gate?.complete(Unit)
            advanceUntilIdle()
            assertEquals(1, categories.refreshCalls)
            assertEquals(CategoriesUiState.Loaded(persistentListOf(GENERAL)), viewModel.uiState.value.categories)
        }

    @Test
    fun `a failure with a cache keeps the list and reports it once`() =
        runTest {
            categories.failNext(DataError.Timeout)
            val viewModel = started(viewModel())

            assertEquals(CategoriesUiState.Loaded(persistentListOf(GENERAL, BOOKS)), viewModel.uiState.value.categories)
            viewModel.effects.test {
                assertEquals(LobbyEffect.ShowMessage(DataError.Timeout.toUiText()), awaitItem())
                expectNoEvents()
            }
        }

    @Test
    fun `pulling forces a refresh and shows the indicator while it runs`() =
        runTest {
            val viewModel = started(viewModel())
            categories.remote = listOf(GENERAL, BOOKS, ANIMALS)
            categories.gate = CompletableDeferred()

            viewModel.onAction(LobbyAction.RefreshPulled)
            viewModel.onAction(LobbyAction.RefreshPulled)
            advanceUntilIdle()
            assertTrue(viewModel.uiState.value.isRefreshing)

            categories.gate?.complete(Unit)
            advanceUntilIdle()
            assertEquals(1, categories.refreshCalls)
            assertFalse(viewModel.uiState.value.isRefreshing)
            assertEquals(
                CategoriesUiState.Loaded(persistentListOf(GENERAL, BOOKS, ANIMALS)),
                viewModel.uiState.value.categories,
            )
        }

    @Test
    fun `a selected category that disappears falls back to any`() =
        runTest {
            val viewModel = started(viewModel())
            viewModel.onAction(LobbyAction.CategorySelected(BOOKS.id))
            advanceUntilIdle()
            assertEquals(BOOKS.id, viewModel.uiState.value.categoryId)

            categories.remote = listOf(GENERAL)
            viewModel.onAction(LobbyAction.RefreshPulled)
            advanceUntilIdle()

            assertNull(viewModel.uiState.value.categoryId)
        }

    private companion object {
        val GENERAL = Category(9, "General Knowledge")
        val BOOKS = Category(10, "Entertainment: Books")
        val ANIMALS = Category(27, "Animals")
    }
}
