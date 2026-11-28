package sk.mpage.lumen.core.work.notification

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import dagger.hilt.android.qualifiers.ApplicationContext
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
    ) : ReminderNotifier {
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

        private fun post(
            channel: String,
            id: Int,
            title: String,
            text: String,
        ): Boolean {
            if (!manager.areNotificationsEnabled() || !isChannelOn(channel)) return false
            val notification =
                NotificationCompat
                    .Builder(context, channel)
                    .setSmallIcon(R.drawable.ic_stat_lumen)
                    .setContentTitle(title)
                    .setContentText(text)
                    .setStyle(NotificationCompat.BigTextStyle().bigText(text))
                    .setContentIntent(openApp())
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

        private fun openApp(): PendingIntent? {
            val intent =
                context.packageManager.getLaunchIntentForPackage(context.packageName)?.apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                } ?: return null
            return PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE)
        }

        private companion object {
            const val STREAK_ID = 1
            const val INACTIVITY_ID = 2
        }
    }
