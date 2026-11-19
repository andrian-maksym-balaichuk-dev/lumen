package sk.mpage.lumen.core.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow
import sk.mpage.lumen.core.database.entity.CategoryEntity
import java.time.Instant

@Dao
internal interface CategoryDao {
    @Query("SELECT * FROM category ORDER BY name")
    fun observeAll(): Flow<List<CategoryEntity>>

    @Query("SELECT MIN(updated_at) FROM category")
    suspend fun oldestUpdate(): Instant?

    @Upsert
    suspend fun upsertAll(categories: List<CategoryEntity>)

    @Query("DELETE FROM category WHERE id NOT IN (:keepIds)")
    suspend fun deleteAllExcept(keepIds: List<Int>)

    @Transaction
    suspend fun replaceAll(categories: List<CategoryEntity>) {
        upsertAll(categories)
        deleteAllExcept(categories.map { it.id })
    }
}
