package sk.mpage.lumen.feature.game.navigation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable
import sk.mpage.lumen.core.model.QuizConfig
import sk.mpage.lumen.feature.game.play.GameRoute
import sk.mpage.lumen.feature.game.result.ResultRoute

@Serializable
public data class GameKey(
    val config: QuizConfig,
) : NavKey

@Serializable
public data class ResultKey(
    val sessionId: Long,
) : NavKey

public fun EntryProviderScope<NavKey>.gameEntries(
    onClose: () -> Unit,
    onShowResults: (sessionId: Long) -> Unit,
    onPlayAgain: (QuizConfig) -> Unit,
    onReview: (sessionId: Long) -> Unit,
) {
    entry<GameKey> { key -> GameRoute(config = key.config, onClose = onClose, onShowResults = onShowResults) }
    entry<ResultKey> { key ->
        ResultRoute(sessionId = key.sessionId, onClose = onClose, onPlayAgain = onPlayAgain, onReview = onReview)
    }
}
