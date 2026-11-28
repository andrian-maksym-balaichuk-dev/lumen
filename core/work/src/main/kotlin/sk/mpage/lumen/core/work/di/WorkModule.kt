package sk.mpage.lumen.core.work.di

import android.content.Context
import androidx.work.WorkManager
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import sk.mpage.lumen.core.domain.port.ReminderNotifier
import sk.mpage.lumen.core.work.notification.SystemTrayNotifier

@Module
@InstallIn(SingletonComponent::class)
internal abstract class WorkModule {
    @Binds
    abstract fun reminderNotifier(notifier: SystemTrayNotifier): ReminderNotifier

    companion object {
        @Provides
        fun workManager(
            @ApplicationContext context: Context,
        ): WorkManager = WorkManager.getInstance(context)
    }
}
