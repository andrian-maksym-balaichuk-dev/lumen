package sk.mpage.lumen.core.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import sk.mpage.lumen.core.common.result.AppResult
import sk.mpage.lumen.core.common.result.DataError
import sk.mpage.lumen.core.common.text.HtmlEntityDecoder
import sk.mpage.lumen.core.data.mapper.toCategory
import sk.mpage.lumen.core.data.mapper.toEntity
import sk.mpage.lumen.core.database.entity.CategoryEntity
import sk.mpage.lumen.core.database.source.CategoryLocalDataSource
import sk.mpage.lumen.core.domain.repository.CategoryRepository
import sk.mpage.lumen.core.model.Category
import sk.mpage.lumen.core.network.OpenTdbRemoteDataSource
import java.time.Clock
import javax.inject.Inject
import kotlin.time.Duration
import kotlin.time.toJavaDuration

internal class OfflineFirstCategoryRepository
    @Inject
    constructor(
        private val local: CategoryLocalDataSource,
        private val remote: OpenTdbRemoteDataSource,
        private val decoder: HtmlEntityDecoder,
        private val clock: Clock,
    ) : CategoryRepository {
        override fun observeCategories(): Flow<List<Category>> =
            local.observeAll().map { categories -> categories.map(CategoryEntity::toCategory) }

        override suspend fun refreshIfStale(maxAge: Duration): AppResult<Unit> {
            val oldest = local.oldestUpdate()
            val fresh = oldest != null && oldest.plus(maxAge.toJavaDuration()).isAfter(clock.instant())
            return if (fresh) AppResult.Success(Unit) else refresh()
        }

        override suspend fun refresh(): AppResult<Unit> {
            val categories =
                when (val result = remote.categories()) {
                    is AppResult.Failure -> return result
                    is AppResult.Success -> result.value
                }
            if (categories.isEmpty()) return AppResult.Failure(DataError.NoResults)
            val now = clock.instant()
            local.replaceAll(categories.map { it.toEntity(decoder, updatedAt = now) })
            return AppResult.Success(Unit)
        }
    }
