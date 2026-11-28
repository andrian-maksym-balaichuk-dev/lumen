package sk.mpage.lumen.core.work.trigger

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import sk.mpage.lumen.core.common.dispatcher.ApplicationScope
import sk.mpage.lumen.core.domain.repository.AppUsageRepository
import java.time.Clock
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
public class AppUsageTracker
    @Inject
    internal constructor(
        private val usage: AppUsageRepository,
        private val clock: Clock,
        @ApplicationScope private val appScope: CoroutineScope,
    ) : DefaultLifecycleObserver {
        override fun onStart(owner: LifecycleOwner) {
            appScope.launch { usage.markActive(clock.instant()) }
        }
    }
