package sk.mpage.lumen.feature.lobby.navigation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable
import sk.mpage.lumen.core.model.QuizConfig
import sk.mpage.lumen.feature.lobby.LobbyRoute

@Serializable
public data object LobbyKey : NavKey

public fun EntryProviderScope<NavKey>.lobbyEntries(
    onOpenProfile: () -> Unit,
    onStartGame: (QuizConfig) -> Unit,
) {
    entry<LobbyKey> { LobbyRoute(onOpenProfile = onOpenProfile, onStartGame = onStartGame) }
}
