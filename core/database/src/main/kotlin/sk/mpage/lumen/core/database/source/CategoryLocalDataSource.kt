package sk.mpage.lumen.core.database.source

import kotlinx.coroutines.flow.Flow
import sk.mpage.lumen.core.database.dao.CategoryDao
import sk.mpage.lumen.core.database.entity.CategoryEntity
import java.time.Instant
import javax.inject.Inject

public class CategoryLocalDataSource
    @Inject
    internal constructor(
        private val dao: CategoryDao,
    ) {
        public fun observeAll(): Flow<List<CategoryEntity>> = dao.observeAll()

        public suspend fun oldestUpdate(): Instant? = dao.oldestUpdate()

        public suspend fun replaceAll(categories: List<CategoryEntity>): Unit = dao.replaceAll(categories)
    }
