package sk.mpage.lumen

import android.app.Application
import androidx.lifecycle.ProcessLifecycleOwner
import dagger.hilt.android.HiltAndroidApp
import sk.mpage.lumen.core.work.notification.NotificationChannels
import sk.mpage.lumen.core.work.trigger.AppUsageTracker
import javax.inject.Inject

@HiltAndroidApp
class LumenApp : Application() {
    @Inject
    lateinit var appUsageTracker: AppUsageTracker

    override fun onCreate() {
        super.onCreate()
        installStrictMode()
        NotificationChannels.create(this)
        ProcessLifecycleOwner.get().lifecycle.addObserver(appUsageTracker)
    }
}
