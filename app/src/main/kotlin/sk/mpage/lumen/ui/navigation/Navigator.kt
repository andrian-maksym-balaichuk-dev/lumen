package sk.mpage.lumen.ui.navigation

import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import sk.mpage.lumen.ui.TopLevelDestination

internal class Navigator(
    private val backStack: NavBackStack<NavKey>,
) {
    val currentTopLevel: TopLevelDestination?
        get() = TopLevelDestination.entries.firstOrNull { it.key == backStack.lastOrNull() }

    fun selectTab(destination: TopLevelDestination) {
        while (backStack.size > 1) backStack.removeAt(backStack.lastIndex)
        if (destination.key != LobbyKey) backStack.add(destination.key)
    }

    fun push(key: NavKey) {
        backStack.add(key)
    }

    fun replaceTop(key: NavKey) {
        backStack[backStack.lastIndex] = key
    }

    fun resetTo(key: NavKey) {
        backStack.clear()
        backStack.add(key)
    }

    fun back() {
        backStack.removeLastOrNull()
    }
}
