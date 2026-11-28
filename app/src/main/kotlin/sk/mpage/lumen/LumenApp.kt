package sk.mpage.lumen

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import sk.mpage.lumen.core.work.notification.NotificationChannels

@HiltAndroidApp
class LumenApp : Application() {
    override fun onCreate() {
        super.onCreate()
        installStrictMode()
        NotificationChannels.create(this)
    }
}
