package sk.mpage.lumen.core.work.notification

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import sk.mpage.lumen.core.domain.port.AchievementNotifier
import sk.mpage.lumen.core.domain.port.ReminderNotifier
import sk.mpage.lumen.core.model.ReminderDecision
import sk.mpage.lumen.core.work.R
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class SystemTrayNotifier
    @Inject
    constructor(
        @ApplicationContext private val context: Context,
    ) : ReminderNotifier,
        AchievementNotifier {
        private val manager = NotificationManagerCompat.from(context)

        override fun areNotificationsAllowed(): Boolean = manager.areNotificationsEnabled()

        override fun showReminder(
            decision: ReminderDecision,
            playerName: String,
        ): Boolean {
            val resources = context.resources
            val (channel, title, text) =
                when (decision) {
                    ReminderDecision.None -> {
                        return false
                    }

                    is ReminderDecision.StreakAtRisk -> {
                        Triple(
                            NotificationChannels.STREAK,
                            resources.getString(R.string.reminder_streak_title),
                            resources.getQuantityString(
                                R.plurals.reminder_streak_text,
                                decision.current,
                                decision.current,
                            ),
                        )
                    }

                    ReminderDecision.StartChallenge -> {
                        Triple(
                            NotificationChannels.STREAK,
                            resources.getString(R.string.reminder_challenge_title),
                            resources.getString(R.string.reminder_challenge_text),
                        )
                    }

                    ReminderDecision.Inactivity -> {
                        Triple(
                            NotificationChannels.INACTIVITY,
                            resources.getString(R.string.reminder_inactivity_title, playerName),
                            resources.getString(R.string.reminder_inactivity_text),
                        )
                    }
                }
            val id = if (channel == NotificationChannels.STREAK) STREAK_ID else INACTIVITY_ID
            return post(channel, id, title, text)
        }

        override fun showMarathonRecord(
            sessionId: Long,
            correct: Int,
            previousBest: Int,
        ) {
            val resources = context.resources
            post(
                channel = NotificationChannels.ACHIEVEMENTS,
                id = RECORD_ID,
                title = resources.getString(R.string.record_title),
                text = resources.getString(R.string.record_text, correct, previousBest),
                intent = openApp(sessionId),
            )
        }

        private fun post(
            channel: String,
            id: Int,
            title: String,
            text: String,
            intent: PendingIntent? = openApp(null),
        ): Boolean {
            if (!manager.areNotificationsEnabled() || !isChannelOn(channel)) return false
            val notification =
                NotificationCompat
                    .Builder(context, channel)
                    .setSmallIcon(R.drawable.ic_stat_lumen)
                    .setContentTitle(title)
                    .setContentText(text)
                    .setStyle(NotificationCompat.BigTextStyle().bigText(text))
                    .setContentIntent(intent)
                    .setAutoCancel(true)
                    .build()
            return try {
                manager.notify(id, notification)
                true
            } catch (_: SecurityException) {
                false
            }
        }

        private fun isChannelOn(channel: String): Boolean =
            manager.getNotificationChannelCompat(channel)?.importance != NotificationManagerCompat.IMPORTANCE_NONE

        private fun openApp(sessionId: Long?): PendingIntent? {
            val intent =
                context.packageManager.getLaunchIntentForPackage(context.packageName)?.apply {
                    flags =
                        Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or
                        Intent.FLAG_ACTIVITY_SINGLE_TOP
                    if (sessionId != null) putExtra(NotificationExtras.SESSION_ID, sessionId)
                } ?: return null
            return PendingIntent.getActivity(
                context,
                if (sessionId != null) RECORD_ID else 0,
                intent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
            )
        }

        private companion object {
            const val STREAK_ID = 1
            const val INACTIVITY_ID = 2
            const val RECORD_ID = 3
        }
    }
