package sk.mpage.lumen.core.work.trigger

import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
public class ReminderScheduler
    @Inject
    internal constructor(
        private val workManager: WorkManager,
    ) {
        public fun ensureScheduled() {
            val request =
                PeriodicWorkRequestBuilder<ReminderWorker>(PERIOD_HOURS, TimeUnit.HOURS, FLEX_HOURS, TimeUnit.HOURS)
                    .setConstraints(Constraints.Builder().setRequiresBatteryNotLow(true).build())
                    .build()
            workManager.enqueueUniquePeriodicWork(WORK_NAME, ExistingPeriodicWorkPolicy.UPDATE, request)
        }

        internal companion object {
            const val WORK_NAME = "reminders"
            const val PERIOD_HOURS = 4L
            const val FLEX_HOURS = 1L
        }
    }
