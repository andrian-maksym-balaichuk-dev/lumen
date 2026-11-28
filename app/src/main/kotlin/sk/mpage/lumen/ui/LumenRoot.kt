package sk.mpage.lumen.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.rememberNavBackStack
import dev.chrisbanes.haze.rememberHazeState
import sk.mpage.lumen.R
import sk.mpage.lumen.core.designsystem.component.AuraBackground
import sk.mpage.lumen.core.designsystem.component.LocalHazeState
import sk.mpage.lumen.core.designsystem.component.LumoraDock
import sk.mpage.lumen.core.designsystem.component.LumoraDockItem
import sk.mpage.lumen.core.designsystem.modifier.hazeBackground
import sk.mpage.lumen.feature.game.navigation.ResultKey
import sk.mpage.lumen.ui.navigation.LumenNavDisplay
import sk.mpage.lumen.ui.navigation.Navigator

@Composable
internal fun LumenRoot(
    startKey: NavKey,
    streakAtRisk: Boolean,
    sessionToOpen: Long?,
    onSessionOpened: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val backStack = rememberNavBackStack(startKey)
    val navigator = remember(backStack) { Navigator(backStack) }
    LaunchedEffect(sessionToOpen) {
        if (sessionToOpen != null) {
            navigator.push(ResultKey(sessionToOpen))
            onSessionOpened()
        }
    }
    val current = navigator.currentTopLevel
    val hazeState = rememberHazeState()

    CompositionLocalProvider(LocalHazeState provides hazeState) {
        Box(modifier = modifier.fillMaxSize()) {
            AuraBackground(Modifier.fillMaxSize().hazeBackground(hazeState))
            LumenNavDisplay(backStack = backStack, navigator = navigator)
            if (current != null) {
                LumoraDock(
                    items =
                        TopLevelDestination.entries.map { destination ->
                            LumoraDockItem(
                                label = stringResource(destination.labelRes),
                                iconRes = destination.iconRes,
                                badgeDescription =
                                    stringResource(R.string.awards_streak_at_risk)
                                        .takeIf { streakAtRisk && destination == TopLevelDestination.AWARDS },
                            )
                        },
                    selectedIndex = TopLevelDestination.entries.indexOf(current),
                    onItemSelected = { navigator.selectTab(TopLevelDestination.entries[it]) },
                    modifier = Modifier.align(Alignment.BottomCenter),
                )
            }
        }
    }
}
