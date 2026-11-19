package sk.mpage.lumen.core.data.repository

import dagger.hilt.android.testing.BindValue
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import mockwebserver3.MockResponse
import mockwebserver3.MockWebServer
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import sk.mpage.lumen.core.common.result.AppResult
import sk.mpage.lumen.core.common.result.DataError
import sk.mpage.lumen.core.database.entity.CategoryEntity
import sk.mpage.lumen.core.database.source.CategoryLocalDataSource
import sk.mpage.lumen.core.domain.repository.CategoryRepository
import sk.mpage.lumen.core.model.Category
import sk.mpage.lumen.core.network.di.OpenTdbBaseUrl
import sk.mpage.lumen.core.network.di.OpenTdbBaseUrlModule
import java.time.Clock
import java.time.Instant
import javax.inject.Inject
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.hours
import kotlin.time.toJavaDuration

@HiltAndroidTest
@UninstallModules(OpenTdbBaseUrlModule::class)
@RunWith(RobolectricTestRunner::class)
class OfflineFirstCategoryRepositoryTest {
    @get:Rule
    val hiltRule = HiltAndroidRule(this)

    private val server = MockWebServer().apply { start() }

    @BindValue
    @OpenTdbBaseUrl
    @JvmField
    val baseUrl: String = server.url("/").toString()

    @Inject
    lateinit var repository: CategoryRepository

    @Inject
    lateinit var local: CategoryLocalDataSource

    @Inject
    lateinit var clock: Clock

    @Before
    fun setUp() {
        hiltRule.inject()
    }

    @After
    fun tearDown() {
        server.close()
    }

    @Test
    fun `a fresh cache skips the network`() =
        runTest {
            local.replaceAll(listOf(cached(9, "General Knowledge", age = 1)))

            val result = repository.refreshIfStale()

            assertEquals(AppResult.Success(Unit), result)
            assertEquals(0, server.requestCount)
            assertEquals(listOf(Category(9, "General Knowledge")), repository.observeCategories().first())
        }

    @Test
    fun `a stale cache is replaced by the api list`() =
        runTest {
            local.replaceAll(listOf(cached(9, "General Knowledge", age = 25), cached(99, "Removed", age = 25)))
            server.enqueue(categories("""{"id":9,"name":"General Knowledge"},{"id":27,"name":"Animals"}"""))

            val result = repository.refreshIfStale()

            assertEquals(AppResult.Success(Unit), result)
            assertEquals("/api_category.php", server.takeRequest().url.encodedPath)
            assertEquals(
                listOf(Category(27, "Animals"), Category(9, "General Knowledge")),
                repository.observeCategories().first(),
            )
        }

    @Test
    fun `an empty cache is fetched`() =
        runTest {
            server.enqueue(categories("""{"id":9,"name":"General Knowledge"}"""))

            assertEquals(AppResult.Success(Unit), repository.refreshIfStale())
            assertEquals(1, server.requestCount)
            assertEquals(listOf(Category(9, "General Knowledge")), repository.observeCategories().first())
        }

    @Test
    fun `a network failure keeps the cached rows`() =
        runTest {
            local.replaceAll(listOf(cached(9, "General Knowledge", age = 25)))
            server.enqueue(MockResponse(code = 503))

            val result = repository.refresh()

            assertEquals(AppResult.Failure(DataError.Http(503)), result)
            assertEquals(listOf(Category(9, "General Knowledge")), repository.observeCategories().first())
        }

    @Test
    fun `an empty api list is a failure and keeps the cached rows`() =
        runTest {
            local.replaceAll(listOf(cached(9, "General Knowledge", age = 25)))
            server.enqueue(categories(""))

            val result = repository.refresh()

            assertEquals(AppResult.Failure(DataError.NoResults), result)
            assertEquals(listOf(Category(9, "General Knowledge")), repository.observeCategories().first())
        }

    @Test
    fun `category names are stored decoded`() =
        runTest {
            server.enqueue(
                categories(
                    """{"id":17,"name":"Science &amp; Nature"},{"id":31,"name":"Entertainment: Japanese Anime &amp; Manga"}""",
                ),
            )

            repository.refresh()

            assertEquals(
                listOf("Entertainment: Japanese Anime & Manga", "Science & Nature"),
                repository.observeCategories().first().map { it.name },
            )
        }

    @Test
    fun `a refresh stamps rows with the current time`() =
        runTest {
            val before = clock.instant()
            server.enqueue(categories("""{"id":9,"name":"General Knowledge"}"""))

            repository.refresh()

            val stamped = requireNotNull(local.oldestUpdate())
            assertTrue(!stamped.isBefore(before) && !stamped.isAfter(clock.instant()))
        }

    private fun cached(
        id: Int,
        name: String,
        age: Int,
    ): CategoryEntity = CategoryEntity(id, name, updatedAt = hoursAgo(age))

    private fun hoursAgo(hours: Int): Instant = clock.instant().minus(hours.hours.toJavaDuration())

    private fun categories(items: String) = MockResponse(body = """{"trivia_categories":[$items]}""")
}
