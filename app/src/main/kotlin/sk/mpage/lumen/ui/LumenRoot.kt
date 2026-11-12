package sk.mpage.lumen.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.rememberNavBackStack
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.rememberHazeState
import sk.mpage.lumen.core.designsystem.component.AuraBackground
import sk.mpage.lumen.core.designsystem.component.GlassSurface
import sk.mpage.lumen.core.designsystem.component.LocalHazeState
import sk.mpage.lumen.ui.navigation.LobbyKey
import sk.mpage.lumen.ui.navigation.LumenNavDisplay
import sk.mpage.lumen.ui.navigation.Navigator

@Composable
internal fun LumenRoot(modifier: Modifier = Modifier) {
    val backStack = rememberNavBackStack(LobbyKey)
    val navigator = remember(backStack) { Navigator(backStack) }
    val current = navigator.currentTopLevel
    val hazeState = rememberHazeState()

    CompositionLocalProvider(LocalHazeState provides hazeState) {
        Box(modifier = modifier.fillMaxSize()) {
            Box(Modifier.fillMaxSize().hazeSource(hazeState)) {
                AuraBackground()
                LumenNavDisplay(backStack = backStack, navigator = navigator)
            }
            if (current != null) {
                GlassSurface(
                    modifier = Modifier.fillMaxWidth().align(Alignment.BottomCenter),
                    shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
                ) {
                    NavigationBar(containerColor = Color.Transparent) {
                        TopLevelDestination.entries.forEach { destination ->
                            NavigationBarItem(
                                selected = destination == current,
                                onClick = { navigator.selectTab(destination) },
                                icon = { Icon(painterResource(destination.iconRes), contentDescription = null) },
                                label = { Text(stringResource(destination.labelRes)) },
                            )
                        }
                    }
                }
            }
        }
    }
}
