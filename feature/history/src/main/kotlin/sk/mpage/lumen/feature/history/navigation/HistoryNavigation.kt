package sk.mpage.lumen.feature.history.navigation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable
import sk.mpage.lumen.feature.history.list.HistoryRoute

@Serializable
public data object HistoryKey : NavKey

public fun EntryProviderScope<NavKey>.historyEntries(onStartQuiz: () -> Unit) {
    entry<HistoryKey> { HistoryRoute(onStartQuiz = onStartQuiz) }
}
