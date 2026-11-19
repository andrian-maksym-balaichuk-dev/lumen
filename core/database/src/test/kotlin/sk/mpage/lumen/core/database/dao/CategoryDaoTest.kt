package sk.mpage.lumen.core.database.dao

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import sk.mpage.lumen.core.database.LumenDatabase
import sk.mpage.lumen.core.database.entity.CategoryEntity
import java.time.Instant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

@RunWith(RobolectricTestRunner::class)
class CategoryDaoTest {
    private lateinit var database: LumenDatabase
    private lateinit var dao: CategoryDao

    @Before
    fun setUp() {
        database =
            Room
                .inMemoryDatabaseBuilder(ApplicationProvider.getApplicationContext(), LumenDatabase::class.java)
                .build()
        dao = database.categoryDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun `observeAll is sorted by name`() =
        runTest {
            dao.upsertAll(
                listOf(
                    category(11, "Film"),
                    category(9, "General Knowledge"),
                    category(27, "Animals"),
                ),
            )

            assertEquals(listOf("Animals", "Film", "General Knowledge"), dao.observeAll().first().map { it.name })
        }

    @Test
    fun `replaceAll updates kept rows, inserts new ones and drops removed ids`() =
        runTest {
            dao.replaceAll(listOf(category(9, "General Knowledge"), category(11, "Film")))

            dao.replaceAll(listOf(category(11, "Film & TV", at = LATER), category(12, "Music", at = LATER)))

            assertEquals(
                listOf(category(11, "Film & TV", at = LATER), category(12, "Music", at = LATER)),
                dao.observeAll().first(),
            )
        }

    @Test
    fun `oldestUpdate returns the earliest timestamp and null for an empty table`() =
        runTest {
            assertNull(dao.oldestUpdate())

            dao.upsertAll(listOf(category(9, "General Knowledge", at = LATER), category(11, "Film")))

            assertEquals(EARLIER, dao.oldestUpdate())
        }

    private fun category(
        id: Int,
        name: String,
        at: Instant = EARLIER,
    ) = CategoryEntity(id = id, name = name, updatedAt = at)

    private companion object {
        val EARLIER: Instant = Instant.ofEpochMilli(1_762_900_000_000)
        val LATER: Instant = Instant.ofEpochMilli(1_762_990_000_000)
    }
}
