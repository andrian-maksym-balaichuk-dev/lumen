package sk.mpage.lumen.core.data.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import sk.mpage.lumen.core.common.dispatcher.Dispatcher
import sk.mpage.lumen.core.common.dispatcher.LumenDispatcher

@Module
@InstallIn(SingletonComponent::class)
internal object CoroutinesModule {
    @Provides
    @Dispatcher(LumenDispatcher.Default)
    fun defaultDispatcher(): CoroutineDispatcher = Dispatchers.Default

    @Provides
    @Dispatcher(LumenDispatcher.IO)
    fun ioDispatcher(): CoroutineDispatcher = Dispatchers.IO
}
