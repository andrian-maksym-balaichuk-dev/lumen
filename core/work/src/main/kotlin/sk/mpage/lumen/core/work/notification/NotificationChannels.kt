package sk.mpage.lumen.core.work.notification

import android.content.Context
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationManagerCompat
import sk.mpage.lumen.core.work.R

public object NotificationChannels {
    public const val STREAK: String = "reminders_streak"
    public const val INACTIVITY: String = "reminders_inactivity"
    public const val ACHIEVEMENTS: String = "achievements"

    public fun create(context: Context) {
        val channels =
            listOf(
                channel(
                    context,
                    STREAK,
                    NotificationManagerCompat.IMPORTANCE_DEFAULT,
                    R.string.channel_streak_name,
                    R.string.channel_streak_description,
                ),
                channel(
                    context,
                    INACTIVITY,
                    NotificationManagerCompat.IMPORTANCE_LOW,
                    R.string.channel_inactivity_name,
                    R.string.channel_inactivity_description,
                ),
                channel(
                    context,
                    ACHIEVEMENTS,
                    NotificationManagerCompat.IMPORTANCE_HIGH,
                    R.string.channel_achievements_name,
                    R.string.channel_achievements_description,
                ),
            )
        NotificationManagerCompat.from(context).createNotificationChannelsCompat(channels)
    }

    private fun channel(
        context: Context,
        id: String,
        importance: Int,
        name: Int,
        description: Int,
    ): NotificationChannelCompat =
        NotificationChannelCompat
            .Builder(id, importance)
            .setName(context.getString(name))
            .setDescription(context.getString(description))
            .build()
}
