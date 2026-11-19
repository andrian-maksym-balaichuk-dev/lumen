package sk.mpage.lumen.core.data.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import sk.mpage.lumen.core.data.repository.OfflineFirstCategoryRepository
import sk.mpage.lumen.core.domain.repository.CategoryRepository

@Module
@InstallIn(SingletonComponent::class)
internal abstract class DataModule {
    @Binds
    abstract fun categoryRepository(repository: OfflineFirstCategoryRepository): CategoryRepository
}
