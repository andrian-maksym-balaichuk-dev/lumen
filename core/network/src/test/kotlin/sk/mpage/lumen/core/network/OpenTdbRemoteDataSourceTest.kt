package sk.mpage.lumen.core.network

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
import java.util.concurrent.TimeUnit
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

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

            val result = assertIs<AppResult.Success<*>>(questions())

            assertEquals(1, (result.value as List<*>).size)
        }

    @Test
    fun `maps open trivia db response codes`() =
        runTest {
            val expected =
                mapOf(
                    1 to DataError.NoResults,
                    2 to DataError.InvalidRequest,
                    3 to DataError.TokenInvalid,
                    4 to DataError.TokenInvalid,
                    5 to DataError.RateLimited,
                    42 to DataError.Unknown,
                )
            expected.forEach { (code, error) ->
                server.enqueue(MockResponse(body = questionsBody(responseCode = code)))

                assertEquals(AppResult.Failure(error), questions(), "response_code $code")
            }
        }

    @Test
    fun `maps http 429 to rate limited and other statuses to http`() =
        runTest {
            server.enqueue(MockResponse(code = 429))
            server.enqueue(MockResponse(code = 503))

            assertEquals(AppResult.Failure(DataError.RateLimited), dataSource().categories())
            assertEquals(AppResult.Failure(DataError.Http(503)), dataSource().categories())
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

            val dataSource = OpenTdbRemoteDataSource(openTdbApi(url, OkHttpClient(), openTdbJson()))

            assertEquals(AppResult.Failure(DataError.NoInternet), dataSource.categories())
        }

    @Test
    fun `malformed json is a serialization error`() =
        runTest {
            server.enqueue(MockResponse(body = """{"trivia_categories":"oops"}"""))

            assertEquals(AppResult.Failure(DataError.Serialization), dataSource().categories())
        }

    private fun dataSource(client: OkHttpClient = OkHttpClient()) =
        OpenTdbRemoteDataSource(openTdbApi(server.url("/"), client, openTdbJson()))

    private suspend fun questions() =
        dataSource().questions(amount = 10, category = null, difficulty = null, type = "multiple", token = null)

    private fun questionsBody(responseCode: Int): String =
        """
        {"response_code":$responseCode,"results":[{"type":"multiple","difficulty":"easy","category":"Science",
        "question":"Q?","correct_answer":"A","incorrect_answers":["B","C","D"]}]}
        """.trimIndent()
}
