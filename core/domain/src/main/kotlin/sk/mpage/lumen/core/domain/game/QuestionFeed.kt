package sk.mpage.lumen.core.domain.game

import sk.mpage.lumen.core.common.result.DataError
import sk.mpage.lumen.core.model.Question

public interface QuestionFeed {
    public suspend fun next(): FeedResult

    public fun close()
}

public sealed interface FeedResult {
    public data class Next(
        val question: Question,
    ) : FeedResult

    public data class Failed(
        val error: DataError,
    ) : FeedResult

    public data object Exhausted : FeedResult
}
