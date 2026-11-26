package sk.mpage.lumen.core.data.feed

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import sk.mpage.lumen.core.common.result.AppResult
import sk.mpage.lumen.core.common.result.DataError
import sk.mpage.lumen.core.domain.game.FeedResult
import sk.mpage.lumen.core.domain.game.GameRules
import sk.mpage.lumen.core.domain.game.QuestionFeed
import sk.mpage.lumen.core.model.Question

internal class ClassicQuestionFeed(
    private val fetch: suspend (amount: Int) -> AppResult<List<Question>>,
) : QuestionFeed {
    private val mutex = Mutex()
    private var questions: List<Question>? = null
    private var served = 0

    override suspend fun next(): FeedResult =
        mutex.withLock {
            val loaded =
                questions ?: when (val result = fetch(GameRules.CLASSIC_QUESTION_COUNT)) {
                    is AppResult.Failure -> return@withLock FeedResult.Failed(result.error)
                    is AppResult.Success -> result.value.also { questions = it }
                }
            val question = loaded.getOrNull(served) ?: return@withLock FeedResult.Failed(DataError.NoResults)
            served++
            FeedResult.Next(question)
        }

    override fun close() {
        questions = null
    }
}
