package sk.mpage.lumen.core.model

import kotlinx.serialization.Serializable

@Serializable
public data class HistoryFilter(
    val categoryId: Int?,
    val mode: GameMode?,
    val sort: HistorySort,
) {
    public companion object {
        public val Default: HistoryFilter = HistoryFilter(categoryId = null, mode = null, sort = HistorySort.NEWEST)
    }
}
