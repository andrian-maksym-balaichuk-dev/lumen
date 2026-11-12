package sk.mpage.lumen.core.network

import kotlinx.coroutines.test.runTest
import mockwebserver3.MockResponse
import mockwebserver3.MockWebServer
import okhttp3.OkHttpClient
import org.junit.After
import org.junit.Before
import sk.mpage.lumen.core.network.di.openTdbApi
import sk.mpage.lumen.core.network.di.openTdbJson
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class OpenTdbApiTest {
    private val server = MockWebServer()
    private lateinit var api: OpenTdbApi

    @Before
    fun setUp() {
        server.start()
        api = openTdbApi(server.url("/"), OkHttpClient(), openTdbJson())
    }

    @After
    fun tearDown() {
        server.close()
    }

    @Test
    fun `parses the recorded category list`() =
        runTest {
            server.enqueue(fixture("api_category.json"))

            val response = api.categories()

            assertEquals(24, response.categories.size)
            assertEquals(9, response.categories.first().id)
            assertEquals("General Knowledge", response.categories.first().name)
            assertEquals("/api_category.php", server.takeRequest().url.encodedPath)
        }

    @Test
    fun `parses the recorded questions`() =
        runTest {
            server.enqueue(fixture("api_questions.json"))

            val response =
                api.questions(
                    amount = 3,
                    category = null,
                    difficulty = null,
                    type = "multiple",
                    token = null,
                )

            assertEquals(0, response.responseCode)
            assertEquals(3, response.results.size)
            val first = response.results.first()
            assertEquals("Where are Terror Fiends more commonly found in the Nintendo game Miitopia?", first.question)
            assertEquals("New Lumos", first.correctAnswer)
            assertEquals(listOf("Peculia", "The Sky Scraper", "Otherworld"), first.incorrectAnswers)
        }

    @Test
    fun `omits null query parameters and always sends the type`() =
        runTest {
            server.enqueue(fixture("api_questions.json"))

            api.questions(amount = 10, category = null, difficulty = null, type = "boolean", token = null)

            val url = server.takeRequest().url
            assertEquals("10", url.queryParameter("amount"))
            assertEquals("boolean", url.queryParameter("type"))
            assertEquals(setOf("amount", "type"), url.queryParameterNames)
        }

    @Test
    fun `parses a token response and ignores unknown keys`() =
        runTest {
            server.enqueue(
                MockResponse(body = """{"response_code":0,"response_message":"Token Generated","token":"abc"}"""),
            )

            val response = api.token(command = "request")

            assertEquals(0, response.responseCode)
            assertEquals("abc", response.token)
            assertEquals("request", server.takeRequest().url.queryParameter("command"))
        }

    @Test
    fun `a response without results parses to an empty list`() =
        runTest {
            server.enqueue(MockResponse(body = """{"response_code":1}"""))

            val response =
                api.questions(
                    amount = 50,
                    category = 9,
                    difficulty = "hard",
                    type = "multiple",
                    token = null,
                )

            assertEquals(1, response.responseCode)
            assertEquals(emptyList(), response.results)
            assertNull(server.takeRequest().url.queryParameter("token"))
        }

    private fun fixture(name: String): MockResponse {
        val body =
            checkNotNull(
                javaClass.classLoader?.getResource("fixtures/$name"),
            ) { "Missing fixture $name" }.readText()
        return MockResponse(body = body)
    }
}
