package sk.mpage.lumen.core.domain.repository

import kotlinx.coroutines.flow.Flow
import sk.mpage.lumen.core.common.result.AppResult
import sk.mpage.lumen.core.model.Category
import kotlin.time.Duration
import kotlin.time.Duration.Companion.hours

public interface CategoryRepository {
    public fun observeCategories(): Flow<List<Category>>

    public suspend fun refreshIfStale(maxAge: Duration = 24.hours): AppResult<Unit>

    public suspend fun refresh(): AppResult<Unit>
}
