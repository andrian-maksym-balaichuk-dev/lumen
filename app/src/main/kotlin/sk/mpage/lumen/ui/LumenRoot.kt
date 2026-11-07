package sk.mpage.lumen.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.navigation3.runtime.rememberNavBackStack
import sk.mpage.lumen.ui.navigation.LobbyKey
import sk.mpage.lumen.ui.navigation.LumenNavDisplay
import sk.mpage.lumen.ui.navigation.Navigator

@Composable
internal fun LumenRoot(modifier: Modifier = Modifier) {
    val backStack = rememberNavBackStack(LobbyKey)
    val navigator = remember(backStack) { Navigator(backStack) }
    val current = navigator.currentTopLevel

    Scaffold(
        modifier = modifier.fillMaxSize(),
        bottomBar = {
            if (current != null) {
                NavigationBar {
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
        },
    ) { innerPadding ->
        LumenNavDisplay(
            backStack = backStack,
            navigator = navigator,
            modifier = Modifier.padding(innerPadding),
        )
    }
}
