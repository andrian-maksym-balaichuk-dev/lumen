package sk.mpage.lumen.core.data.feed

import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.plus
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import sk.mpage.lumen.core.common.result.AppResult
import sk.mpage.lumen.core.common.result.DataError
import sk.mpage.lumen.core.domain.game.FeedResult
import sk.mpage.lumen.core.domain.game.QuestionFeed
import sk.mpage.lumen.core.model.Question

internal class MarathonQuestionFeed(
    scope: CoroutineScope,
    private val fetch: suspend (amount: Int) -> AppResult<List<Question>>,
) : QuestionFeed {
    private val job = SupervisorJob(scope.coroutineContext[Job])
    private val feedScope = scope + job
    private val mutex = Mutex()
    private val buffer = ArrayDeque<Question>()
    private val seen = mutableSetOf<String>()
    private var refill: Deferred<Refill>? = null

    override suspend fun next(): FeedResult {
        while (true) {
            val pending =
                mutex.withLock {
                    val question = buffer.removeFirstOrNull()
                    if (question != null) {
                        if (buffer.size <= REFILL_THRESHOLD) startRefillLocked()
                        return FeedResult.Next(question)
                    }
                    startRefillLocked()
                }
            when (val outcome = pending.await()) {
                Refill.Added -> {
                    Unit
                }

                Refill.Exhausted -> {
                    mutex.withLock { if (buffer.isEmpty()) return FeedResult.Exhausted }
                }

                is Refill.Error -> {
                    mutex.withLock { if (buffer.isEmpty()) return FeedResult.Failed(outcome.error) }
                }
            }
        }
    }

    override fun close() {
        job.cancel()
    }

    private fun startRefillLocked(): Deferred<Refill> {
        refill?.takeIf { it.isActive }?.let { return it }
        Log.d(TAG, "refill started with ${buffer.size} buffered")
        return feedScope.async { refillOnce() }.also { refill = it }
    }

    private suspend fun refillOnce(): Refill {
        repeat(FETCH_ATTEMPTS) {
            val questions =
                when (val result = fetch(BATCH_SIZE)) {
                    is AppResult.Failure -> return Refill.Error(result.error)
                    is AppResult.Success -> result.value
                }
            val added =
                mutex.withLock {
                    val fresh =
                        questions
                            .filter { it.id !in seen }
                            .distinctBy { it.id }
                            .take(MAX_BUFFER - buffer.size)
                    buffer.addAll(fresh)
                    seen.addAll(fresh.map { it.id })
                    fresh.size
                }
            if (added > 0) return Refill.Added
        }
        return Refill.Exhausted
    }

    private sealed interface Refill {
        data object Added : Refill

        data object Exhausted : Refill

        data class Error(
            val error: DataError,
        ) : Refill
    }

    companion object {
        const val BATCH_SIZE = 50
        const val REFILL_THRESHOLD = 10
        const val MAX_BUFFER = 60
        private const val FETCH_ATTEMPTS = 2
        private const val TAG = "MarathonFeed"
    }
}
