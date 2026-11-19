package sk.mpage.lumen.core.network.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Qualifier

@Qualifier
@Retention(AnnotationRetention.BINARY)
public annotation class OpenTdbBaseUrl

@Module
@InstallIn(SingletonComponent::class)
public object OpenTdbBaseUrlModule {
    @Provides
    @OpenTdbBaseUrl
    public fun baseUrl(): String = "https://opentdb.com/"
}
