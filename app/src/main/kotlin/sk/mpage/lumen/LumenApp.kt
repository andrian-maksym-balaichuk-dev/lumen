package sk.mpage.lumen

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class LumenApp : Application() {
    override fun onCreate() {
        super.onCreate()
        installStrictMode()
    }
}
