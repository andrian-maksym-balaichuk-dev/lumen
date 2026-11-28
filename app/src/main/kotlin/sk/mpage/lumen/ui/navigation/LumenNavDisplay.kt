package sk.mpage.lumen.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import sk.mpage.lumen.feature.awards.navigation.awardsEntries
import sk.mpage.lumen.feature.game.navigation.GameKey
import sk.mpage.lumen.feature.game.navigation.ResultKey
import sk.mpage.lumen.feature.game.navigation.gameEntries
import sk.mpage.lumen.feature.history.navigation.SessionDetailKey
import sk.mpage.lumen.feature.history.navigation.historyEntries
import sk.mpage.lumen.feature.lobby.navigation.LobbyKey
import sk.mpage.lumen.feature.lobby.navigation.lobbyEntries
import sk.mpage.lumen.feature.profile.navigation.profileEntries
import sk.mpage.lumen.ui.TopLevelDestination

@Composable
internal fun LumenNavDisplay(
    backStack: NavBackStack<NavKey>,
    navigator: Navigator,
    modifier: Modifier = Modifier,
) {
    NavDisplay(
        backStack = backStack,
        onBack = navigator::back,
        modifier = modifier,
        entryDecorators =
            listOf(
                rememberSaveableStateHolderNavEntryDecorator(),
                rememberViewModelStoreNavEntryDecorator(),
            ),
        entryProvider =
            entryProvider {
                profileEntries(onOnboardingDone = { navigator.resetTo(LobbyKey) })
                lobbyEntries(
                    onOpenProfile = { navigator.selectTab(TopLevelDestination.PROFILE) },
                    onStartGame = { config -> navigator.push(GameKey(config)) },
                )
                gameEntries(
                    onClose = navigator::back,
                    onShowResults = { sessionId -> navigator.replaceTop(ResultKey(sessionId)) },
                    onPlayAgain = { config -> navigator.replaceTop(GameKey(config)) },
                    onReview = { sessionId -> navigator.push(SessionDetailKey(sessionId)) },
                )
                historyEntries(
                    onStartQuiz = { navigator.selectTab(TopLevelDestination.PLAY) },
                    onOpenSession = { sessionId -> navigator.push(SessionDetailKey(sessionId)) },
                    onBack = navigator::back,
                )
                awardsEntries()
            },
    )
}
