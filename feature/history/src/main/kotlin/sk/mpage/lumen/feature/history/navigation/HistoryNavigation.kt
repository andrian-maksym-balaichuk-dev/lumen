package sk.mpage.lumen.feature.history.navigation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable
import sk.mpage.lumen.feature.history.detail.SessionDetailRoute
import sk.mpage.lumen.feature.history.list.HistoryRoute

@Serializable
public data object HistoryKey : NavKey

@Serializable
public data class SessionDetailKey(
    val sessionId: Long,
) : NavKey

public fun EntryProviderScope<NavKey>.historyEntries(
    onStartQuiz: () -> Unit,
    onOpenSession: (sessionId: Long) -> Unit,
    onBack: () -> Unit,
) {
    entry<HistoryKey> { HistoryRoute(onStartQuiz = onStartQuiz, onOpenSession = onOpenSession) }
    entry<SessionDetailKey> { key -> SessionDetailRoute(sessionId = key.sessionId, onBack = onBack) }
}
