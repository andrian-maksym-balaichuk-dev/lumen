package sk.mpage.lumen.core.testing.feed

import sk.mpage.lumen.core.common.result.DataError
import sk.mpage.lumen.core.domain.game.FeedResult
import sk.mpage.lumen.core.domain.game.QuestionFeed
import sk.mpage.lumen.core.model.Question

public class FakeQuestionFeed(
    questions: List<Question>,
) : QuestionFeed {
    private val remaining = ArrayDeque(questions)
    private val failures = ArrayDeque<DataError>()

    public var closed: Boolean = false
        private set

    public fun failNext(error: DataError) {
        failures.addLast(error)
    }

    override suspend fun next(): FeedResult {
        failures.removeFirstOrNull()?.let { return FeedResult.Failed(it) }
        return remaining.removeFirstOrNull()?.let(FeedResult::Next) ?: FeedResult.Exhausted
    }

    override fun close() {
        closed = true
    }
}
