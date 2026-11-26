package sk.mpage.lumen.core.testing.repository

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import sk.mpage.lumen.core.common.result.AppResult
import sk.mpage.lumen.core.common.result.DataError
import sk.mpage.lumen.core.domain.repository.CategoryRepository
import sk.mpage.lumen.core.model.Category
import kotlin.time.Duration

public class FakeCategoryRepository(
    cached: List<Category> = emptyList(),
    public var remote: List<Category> = cached,
) : CategoryRepository {
    private val categories = MutableStateFlow(cached)
    private val failures = ArrayDeque<DataError>()

    public var gate: CompletableDeferred<Unit>? = null
    public var refreshIfStaleCalls: Int = 0
        private set
    public var refreshCalls: Int = 0
        private set

    public fun failNext(error: DataError) {
        failures.addLast(error)
    }

    override fun observeCategories(): Flow<List<Category>> = categories

    override suspend fun refreshIfStale(maxAge: Duration): AppResult<Unit> {
        refreshIfStaleCalls++
        return load()
    }

    override suspend fun refresh(): AppResult<Unit> {
        refreshCalls++
        return load()
    }

    private suspend fun load(): AppResult<Unit> {
        gate?.await()
        failures.removeFirstOrNull()?.let { return AppResult.Failure(it) }
        categories.value = remote
        return AppResult.Success(Unit)
    }
}
