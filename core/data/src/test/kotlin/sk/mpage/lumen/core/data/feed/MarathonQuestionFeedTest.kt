package sk.mpage.lumen.core.data.feed

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import sk.mpage.lumen.core.common.result.AppResult
import sk.mpage.lumen.core.common.result.DataError
import sk.mpage.lumen.core.domain.game.FeedResult
import sk.mpage.lumen.core.model.Difficulty
import sk.mpage.lumen.core.model.Question
import sk.mpage.lumen.core.model.QuestionType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

@RunWith(RobolectricTestRunner::class)
class MarathonQuestionFeedTest {
    private val requests = mutableListOf<Int>()
    private val responses = ArrayDeque<AppResult<List<Question>>>()
    private var gate: CompletableDeferred<Unit>? = null

    private fun TestScope.feed() =
        MarathonQuestionFeed(backgroundScope) { amount ->
            requests += amount
            gate?.await()
            responses.removeFirstOrNull() ?: AppResult.Failure(DataError.NoInternet)
        }

    private suspend fun MarathonQuestionFeed.take(count: Int): List<Question> =
        List(count) { assertIs<FeedResult.Next>(next()).question }

    @Test
    fun `the first question waits for one batch of 50`() =
        runTest {
            responses += batch(1..50)

            assertEquals("q1", assertIs<FeedResult.Next>(feed().next()).question.id)
            assertEquals(listOf(50), requests)
        }

    @Test
    fun `a refill starts once ten questions remain`() =
        runTest {
            responses += batch(1..50)
            responses += batch(51..100)
            val feed = feed()

            feed.take(39)
            runCurrent()
            assertEquals(1, requests.size)

            feed.take(1)
            runCurrent()
            assertEquals(listOf(50, 50), requests)
            assertEquals((41..60).map { "q$it" }, feed.take(20).map { it.id })
        }

    @Test
    fun `concurrent requests share one fetch`() =
        runTest {
            gate = CompletableDeferred()
            responses += batch(1..50)
            val feed = feed()

            val results = List(5) { async { feed.next() } }
            runCurrent()
            gate?.complete(Unit)

            assertEquals(
                5,
                results
                    .awaitAll()
                    .map { assertIs<FeedResult.Next>(it).question.id }
                    .toSet()
                    .size,
            )
            assertEquals(1, requests.size)
        }

    @Test
    fun `duplicates within a token cycle are dropped`() =
        runTest {
            responses += batch(1..50)
            responses += batch(26..75)
            val feed = feed()

            val ids = feed.take(65).map { it.id }

            assertEquals(65, ids.toSet().size)
            assertEquals((1..65).map { "q$it" }, ids)
        }

    @Test
    fun `a used up question bank is retried once and then ends instead of repeating`() =
        runTest {
            responses += batch(1..50)
            responses += batch(1..50)
            responses += batch(1..50)
            val feed = feed()

            feed.take(50)

            assertEquals(FeedResult.Failed(DataError.NoResults), feed.next())
            assertEquals(3, requests.size)
        }

    @Test
    fun `a question is never asked twice in one game`() =
        runTest {
            responses += batch(1..50)
            responses += batch(1..60)
            val feed = feed()

            val ids = feed.take(60).map { it.id }

            assertEquals((1..60).map { "q$it" }, ids)
        }

    @Test
    fun `buffered questions keep playing after a background failure`() =
        runTest {
            responses += batch(1..50)
            val feed = feed()

            feed.take(45)
            runCurrent()
            assertEquals(5, feed.take(5).size)
            assertEquals(FeedResult.Failed(DataError.NoInternet), feed.next())
            assertTrue(requests.size >= 2)
        }

    @Test
    fun `closing the feed cancels a refill in flight`() =
        runTest {
            gate = CompletableDeferred()
            responses += batch(1..50)
            val feed = feed()
            val pending = async { feed.next() }
            runCurrent()

            feed.close()
            runCurrent()

            assertTrue(pending.isCancelled || pending.isCompleted)
        }

    private fun batch(ids: IntRange): AppResult<List<Question>> =
        AppResult.Success(
            ids.map {
                Question(
                    "q$it",
                    "Q$it",
                    listOf("a", "b", "c", "d"),
                    "a",
                    "Any",
                    Difficulty.EASY,
                    QuestionType.MULTIPLE_CHOICE,
                )
            },
        )
}
