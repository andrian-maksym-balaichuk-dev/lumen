package sk.mpage.lumen.feature.game.navigation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable
import sk.mpage.lumen.core.model.QuizConfig
import sk.mpage.lumen.feature.game.play.GameRoute

@Serializable
public data class GameKey(
    val config: QuizConfig,
) : NavKey

public fun EntryProviderScope<NavKey>.gameEntries(onClose: () -> Unit) {
    entry<GameKey> { key -> GameRoute(config = key.config, onClose = onClose) }
}
