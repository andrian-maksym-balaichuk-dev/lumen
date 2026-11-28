package sk.mpage.lumen.core.work.trigger

import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.workDataOf
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import sk.mpage.lumen.core.domain.port.ReminderCheck
import sk.mpage.lumen.core.model.ManualCheck
import sk.mpage.lumen.core.model.ReminderStatus
import java.time.Instant
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
public class ReminderScheduler
    @Inject
    internal constructor(
        private val workManager: WorkManager,
    ) : ReminderCheck {
        public fun ensureScheduled() {
            val request =
                PeriodicWorkRequestBuilder<ReminderWorker>(PERIOD_HOURS, TimeUnit.HOURS, FLEX_HOURS, TimeUnit.HOURS)
                    .setConstraints(Constraints.Builder().setRequiresBatteryNotLow(true).build())
                    .build()
            workManager.enqueueUniquePeriodicWork(WORK_NAME, ExistingPeriodicWorkPolicy.UPDATE, request)
        }

        override val status: Flow<ReminderStatus> =
            combine(
                workManager.getWorkInfosForUniqueWorkFlow(WORK_NAME),
                workManager.getWorkInfosForUniqueWorkFlow(NOW_WORK_NAME),
            ) { periodic, manual ->
                ReminderStatus(
                    nextCheckAt =
                        periodic
                            .firstOrNull { it.state == WorkInfo.State.ENQUEUED }
                            ?.nextScheduleTimeMillis
                            ?.takeIf { it != Long.MAX_VALUE }
                            ?.let(Instant::ofEpochMilli),
                    manualCheck = manual.lastOrNull()?.toManualCheck() ?: ManualCheck.IDLE,
                )
            }

        override fun runNow(): Flow<ManualCheck> {
            val request =
                OneTimeWorkRequestBuilder<ReminderWorker>()
                    .setInputData(workDataOf(ReminderWorker.KEY_FORCE to true))
                    .build()
            workManager.enqueueUniqueWork(NOW_WORK_NAME, ExistingWorkPolicy.REPLACE, request)
            return workManager.getWorkInfoByIdFlow(request.id).filterNotNull().map { it.toManualCheck() }
        }

        private fun WorkInfo.toManualCheck(): ManualCheck =
            when (state) {
                WorkInfo.State.ENQUEUED, WorkInfo.State.BLOCKED -> {
                    ManualCheck.QUEUED
                }

                WorkInfo.State.RUNNING -> {
                    ManualCheck.RUNNING
                }

                WorkInfo.State.SUCCEEDED -> {
                    outputData
                        .getString(ReminderWorker.KEY_OUTCOME)
                        ?.let { name -> ManualCheck.entries.firstOrNull { it.name == name } }
                        ?: ManualCheck.NOTHING_TO_REMIND
                }

                WorkInfo.State.FAILED, WorkInfo.State.CANCELLED -> {
                    ManualCheck.IDLE
                }
            }

        internal companion object {
            const val WORK_NAME = "reminders"
            const val NOW_WORK_NAME = "reminders_now"
            const val PERIOD_HOURS = 4L
            const val FLEX_HOURS = 1L
        }
    }
