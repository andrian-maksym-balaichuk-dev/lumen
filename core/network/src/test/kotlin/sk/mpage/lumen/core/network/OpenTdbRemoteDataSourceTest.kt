package sk.mpage.lumen.core.network

import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import mockwebserver3.MockResponse
import mockwebserver3.MockWebServer
import okhttp3.OkHttpClient
import org.junit.After
import org.junit.Before
import sk.mpage.lumen.core.common.result.AppResult
import sk.mpage.lumen.core.common.result.DataError
import sk.mpage.lumen.core.network.di.openTdbApi
import sk.mpage.lumen.core.network.di.openTdbJson
import sk.mpage.lumen.core.network.model.RemoteCategory
import sk.mpage.lumen.core.network.model.RemoteQuestions
import sk.mpage.lumen.core.testing.TestMonotonicClock
import java.util.concurrent.TimeUnit
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class OpenTdbRemoteDataSourceTest {
    private val server = MockWebServer()

    @Before
    fun setUp() {
        server.start()
    }

    @After
    fun tearDown() {
        server.close()
    }

    @Test
    fun `maps categories to remote values`() =
        runTest {
            server.enqueue(MockResponse(body = """{"trivia_categories":[{"id":9,"name":"General Knowledge"}]}"""))

            assertEquals(AppResult.Success(listOf(RemoteCategory(9, "General Knowledge"))), dataSource().categories())
        }

    @Test
    fun `returns questions for response code 0`() =
        runTest {
            server.enqueue(MockResponse(body = questionsBody(responseCode = 0)))

            val result = assertIs<AppResult.Success<RemoteQuestions>>(questions())

            assertEquals(1, assertIs<RemoteQuestions.Loaded>(result.value).questions.size)
        }

    @Test
    fun `maps open trivia db response codes`() =
        runTest {
            val expected =
                mapOf(
                    1 to DataError.NoResults,
                    2 to DataError.InvalidRequest,
                    5 to DataError.RateLimited,
                    42 to DataError.Unknown,
                )
            val dataSource = dataSource()
            expected.forEach { (code, error) ->
                server.enqueue(MockResponse(body = questionsBody(responseCode = code)))

                assertEquals(AppResult.Failure(error), questions(dataSource), "response_code $code")
            }
        }

    @Test
    fun `token codes are results the caller recovers from`() =
        runTest {
            val dataSource = dataSource()
            server.enqueue(MockResponse(body = questionsBody(responseCode = 3)))
            server.enqueue(MockResponse(body = questionsBody(responseCode = 4)))

            assertEquals(AppResult.Success(RemoteQuestions.TokenNotFound), questions(dataSource))
            assertEquals(AppResult.Success(RemoteQuestions.TokenExhausted), questions(dataSource))
        }

    @Test
    fun `requests and resets a session token`() =
        runTest {
            val dataSource = dataSource()
            server.enqueue(MockResponse(body = """{"response_code":0,"token":"abc"}"""))
            server.enqueue(MockResponse(body = """{"response_code":0,"token":"abc"}"""))

            assertEquals(AppResult.Success("abc"), dataSource.requestToken())
            assertEquals("/api_token.php?command=request", server.takeRequest().target)
            assertEquals(AppResult.Success("abc"), dataSource.resetToken("abc"))
            assertEquals("/api_token.php?command=reset&token=abc", server.takeRequest().target)
        }

    @Test
    fun `maps http 429 to rate limited and other statuses to http`() =
        runTest {
            server.enqueue(MockResponse(code = 429))
            server.enqueue(MockResponse(code = 503))

            val dataSource = dataSource()

            assertEquals(AppResult.Failure(DataError.RateLimited), dataSource.categories())
            assertEquals(AppResult.Failure(DataError.Http(503)), dataSource.categories())
        }

    @Test
    fun `a slow body times out`() =
        runTest {
            server.enqueue(
                MockResponse
                    .Builder()
                    .body("""{"trivia_categories":[]}""")
                    .bodyDelay(2, TimeUnit.SECONDS)
                    .build(),
            )
            val client = OkHttpClient.Builder().readTimeout(200, TimeUnit.MILLISECONDS).build()

            assertEquals(AppResult.Failure(DataError.Timeout), dataSource(client).categories())
        }

    @Test
    fun `the overall call timeout maps to timeout`() =
        runTest {
            server.enqueue(
                MockResponse
                    .Builder()
                    .body("""{"trivia_categories":[]}""")
                    .headersDelay(2, TimeUnit.SECONDS)
                    .build(),
            )
            val client = OkHttpClient.Builder().callTimeout(200, TimeUnit.MILLISECONDS).build()

            assertEquals(AppResult.Failure(DataError.Timeout), dataSource(client).categories())
        }

    @Test
    fun `a closed port means no internet`() =
        runTest {
            val closed = MockWebServer().apply { start() }
            val url = closed.url("/")
            closed.close()

            val dataSource = OpenTdbRemoteDataSource(openTdbApi(url, OkHttpClient(), openTdbJson()), limiter())

            assertEquals(AppResult.Failure(DataError.NoInternet), dataSource.categories())
        }

    @Test
    fun `consecutive calls are spaced by the rate limiter`() =
        runTest {
            server.enqueue(MockResponse(body = """{"trivia_categories":[]}"""))
            server.enqueue(MockResponse(body = questionsBody(responseCode = 0)))
            val dataSource = dataSource()

            dataSource.categories()
            val firstEnded = testScheduler.currentTime
            questions(dataSource)

            assertTrue(testScheduler.currentTime - firstEnded >= OpenTdbRateLimiter.MIN_INTERVAL_MS)
            assertEquals(2, server.requestCount)
        }

    @Test
    fun `malformed json is a serialization error`() =
        runTest {
            server.enqueue(MockResponse(body = """{"trivia_categories":"oops"}"""))

            assertEquals(AppResult.Failure(DataError.Serialization), dataSource().categories())
        }

    private fun TestScope.limiter() = OpenTdbRateLimiter(TestMonotonicClock(testScheduler))

    private fun TestScope.dataSource(client: OkHttpClient = OkHttpClient()) =
        OpenTdbRemoteDataSource(openTdbApi(server.url("/"), client, openTdbJson()), limiter())

    private suspend fun TestScope.questions() = questions(dataSource())

    private suspend fun questions(dataSource: OpenTdbRemoteDataSource) =
        dataSource.questions(amount = 10, category = null, difficulty = null, type = "multiple", token = null)

    private fun questionsBody(responseCode: Int): String =
        """
        {"response_code":$responseCode,"results":[{"type":"multiple","difficulty":"easy","category":"Science",
        "question":"Q?","correct_answer":"A","incorrect_answers":["B","C","D"]}]}
        """.trimIndent()
}
