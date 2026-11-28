package sk.mpage.lumen

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.work.Configuration
import dagger.hilt.android.HiltAndroidApp
import sk.mpage.lumen.core.work.notification.NotificationChannels
import sk.mpage.lumen.core.work.trigger.AppUsageTracker
import sk.mpage.lumen.core.work.trigger.ReminderScheduler
import javax.inject.Inject

@HiltAndroidApp
class LumenApp :
    Application(),
    Configuration.Provider {
    @Inject
    lateinit var appUsageTracker: AppUsageTracker

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    @Inject
    lateinit var reminderScheduler: ReminderScheduler

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder().setWorkerFactory(workerFactory).build()

    override fun onCreate() {
        super.onCreate()
        installStrictMode()
        NotificationChannels.create(this)
        ProcessLifecycleOwner.get().lifecycle.addObserver(appUsageTracker)
        reminderScheduler.ensureScheduled()
    }
}
