package sk.mpage.lumen.core.data.repository

import dagger.hilt.android.testing.BindValue
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import mockwebserver3.MockResponse
import mockwebserver3.MockWebServer
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import sk.mpage.lumen.core.common.result.DataError
import sk.mpage.lumen.core.domain.game.FeedResult
import sk.mpage.lumen.core.domain.repository.QuestionRepository
import sk.mpage.lumen.core.model.Difficulty
import sk.mpage.lumen.core.model.GameMode
import sk.mpage.lumen.core.model.QuestionType
import sk.mpage.lumen.core.model.QuizConfig
import sk.mpage.lumen.core.network.di.OpenTdbBaseUrl
import sk.mpage.lumen.core.network.di.OpenTdbBaseUrlModule
import javax.inject.Inject
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

@HiltAndroidTest
@UninstallModules(OpenTdbBaseUrlModule::class)
@RunWith(RobolectricTestRunner::class)
class OpenTdbQuestionRepositoryTest {
    @get:Rule
    val hiltRule = HiltAndroidRule(this)

    private val server = MockWebServer().apply { start() }

    @BindValue
    @OpenTdbBaseUrl
    @JvmField
    val baseUrl: String = server.url("/").toString()

    @Inject
    lateinit var repository: QuestionRepository

    @Before
    fun setUp() {
        hiltRule.inject()
    }

    @After
    fun tearDown() {
        server.close()
    }

    @Test
    fun `a classic feed serves ten decoded questions from one request`() =
        runTest {
            server.enqueue(token("t1"))
            server.enqueue(questions(code = 0))
            val feed = feed()

            val questions = List(10) { assertIs<FeedResult.Next>(feed.next()).question }

            assertEquals("Question 1 &", questions.first().text)
            assertEquals(10, questions.map { it.id }.toSet().size)
            assertIs<FeedResult.Failed>(feed.next())
            assertEquals("/api_token.php?command=request", server.takeRequest().target)
            assertEquals(
                "/api.php?amount=10&category=9&difficulty=hard&type=multiple&token=t1",
                server.takeRequest().target,
            )
            assertEquals(2, server.requestCount)
        }

    @Test
    fun `an unknown token is replaced and the request retried`() =
        runTest {
            server.enqueue(token("t1"))
            server.enqueue(questions(code = 3))
            server.enqueue(token("t2"))
            server.enqueue(questions(code = 0))

            assertIs<FeedResult.Next>(feed().next())
            server.takeRequest()
            assertTrue(server.takeRequest().target.endsWith("token=t1"))
            assertEquals("/api_token.php?command=request", server.takeRequest().target)
            assertTrue(server.takeRequest().target.endsWith("token=t2"))
        }

    @Test
    fun `an exhausted token is reset and the request retried`() =
        runTest {
            server.enqueue(token("t1"))
            server.enqueue(questions(code = 4))
            server.enqueue(token("t1"))
            server.enqueue(questions(code = 0))

            assertIs<FeedResult.Next>(feed().next())
            server.takeRequest()
            server.takeRequest()
            assertEquals("/api_token.php?command=reset&token=t1", server.takeRequest().target)
            assertEquals(4, server.requestCount)
        }

    @Test
    fun `a failed token request still plays without a token`() =
        runTest {
            server.enqueue(MockResponse(code = 503))
            server.enqueue(questions(code = 0))

            assertIs<FeedResult.Next>(feed().next())
            server.takeRequest()
            assertTrue("token" !in server.takeRequest().target)
        }

    @Test
    fun `too few questions is no results`() =
        runTest {
            server.enqueue(token("t1"))
            server.enqueue(questions(code = 0, count = 7))

            assertEquals(FeedResult.Failed(DataError.NoResults), feed().next())
        }

    private fun TestScope.feed() =
        repository.feed(
            QuizConfig(GameMode.CLASSIC, 9, "General Knowledge", Difficulty.HARD, QuestionType.MULTIPLE_CHOICE),
            backgroundScope,
        )

    private fun token(value: String) = MockResponse(body = """{"response_code":0,"token":"$value"}""")

    private fun questions(
        code: Int,
        count: Int = 10,
    ): MockResponse {
        val results =
            (1..count).joinToString(",") { i ->
                """{"type":"multiple","difficulty":"hard","category":"General Knowledge",
                "question":"Question $i &amp;","correct_answer":"A$i","incorrect_answers":["B","C","D"]}"""
            }
        val body =
            if (code ==
                0
            ) {
                """{"response_code":0,"results":[$results]}"""
            } else {
                """{"response_code":$code,"results":[]}"""
            }
        return MockResponse(body = body)
    }
}
