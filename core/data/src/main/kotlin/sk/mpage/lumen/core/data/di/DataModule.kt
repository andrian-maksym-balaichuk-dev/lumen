package sk.mpage.lumen.core.data.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import sk.mpage.lumen.core.data.network.ConnectivityNetworkMonitor
import sk.mpage.lumen.core.data.repository.DataStoreSettingsRepository
import sk.mpage.lumen.core.data.repository.DataStoreUserProfileRepository
import sk.mpage.lumen.core.data.repository.OfflineFirstCategoryRepository
import sk.mpage.lumen.core.data.repository.OpenTdbQuestionRepository
import sk.mpage.lumen.core.domain.port.NetworkMonitor
import sk.mpage.lumen.core.domain.repository.CategoryRepository
import sk.mpage.lumen.core.domain.repository.QuestionRepository
import sk.mpage.lumen.core.domain.repository.SettingsRepository
import sk.mpage.lumen.core.domain.repository.UserProfileRepository

@Module
@InstallIn(SingletonComponent::class)
internal abstract class DataModule {
    @Binds
    abstract fun categoryRepository(repository: OfflineFirstCategoryRepository): CategoryRepository

    @Binds
    abstract fun userProfileRepository(repository: DataStoreUserProfileRepository): UserProfileRepository

    @Binds
    abstract fun networkMonitor(monitor: ConnectivityNetworkMonitor): NetworkMonitor

    @Binds
    abstract fun questionRepository(repository: OpenTdbQuestionRepository): QuestionRepository

    @Binds
    abstract fun settingsRepository(repository: DataStoreSettingsRepository): SettingsRepository
}
