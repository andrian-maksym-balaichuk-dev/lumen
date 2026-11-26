package sk.mpage.lumen.feature.game.play

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import org.junit.Rule
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import sk.mpage.lumen.core.common.result.DataError
import sk.mpage.lumen.core.designsystem.theme.LumoraTheme
import kotlin.test.Test
import kotlin.test.assertEquals

@RunWith(RobolectricTestRunner::class)
class GameScreenStalledTest {
    @get:Rule
    val compose = createComposeRule()

    @Test
    fun `a stalled marathon offers retry and quit and discard`() {
        val actions = mutableListOf<GameAction>()
        compose.setContent {
            LumoraTheme(darkTheme = false) {
                GameScreen(
                    state = GameUiState.Stalled(DataError.NoInternet),
                    remainingMs = { 7_000 },
                    onAction = { actions += it },
                    onClose = {},
                )
            }
        }

        compose.onNodeWithText("Connection lost").assertIsDisplayed()
        compose.onNodeWithText("Retry").performClick()
        compose.onNodeWithText("Quit and discard").performClick()

        assertEquals(listOf(GameAction.RetryTapped, GameAction.QuitConfirmed), actions)
    }
}
