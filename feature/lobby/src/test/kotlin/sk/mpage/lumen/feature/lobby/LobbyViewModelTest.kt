package sk.mpage.lumen.feature.lobby

import app.cash.turbine.test
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import sk.mpage.lumen.core.model.Avatar
import sk.mpage.lumen.core.model.Difficulty
import sk.mpage.lumen.core.model.GameMode
import sk.mpage.lumen.core.model.QuestionType
import sk.mpage.lumen.core.model.UserProfile
import sk.mpage.lumen.core.testing.MainDispatcherRule
import sk.mpage.lumen.core.testing.repository.FakeUserProfileRepository
import java.time.Clock
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneOffset
import kotlin.test.Test
import kotlin.test.assertEquals

class LobbyViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val profiles = FakeUserProfileRepository(UserProfile("Ada", Avatar.Preset("owl"), onboarded = true))

    private fun viewModel(at: LocalTime = LocalTime.NOON): LobbyViewModel {
        val instant = LocalDateTime.of(2026, 11, 25, at.hour, at.minute).toInstant(ZoneOffset.UTC)
        return LobbyViewModel(profiles, Clock.fixed(instant, ZoneOffset.UTC))
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
}
