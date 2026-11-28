package sk.mpage.lumen.core.work.notification

import android.app.NotificationManager
import android.content.Context
import androidx.core.content.getSystemService
import androidx.test.core.app.ApplicationProvider
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.test.Test
import kotlin.test.assertEquals

@RunWith(RobolectricTestRunner::class)
class NotificationChannelsTest {
    private val context: Context = ApplicationProvider.getApplicationContext()
    private val manager = context.getSystemService<NotificationManager>()!!

    @Test
    fun `creates the three channels with their importance`() {
        NotificationChannels.create(context)
        NotificationChannels.create(context)

        val channels = manager.notificationChannels.associateBy { it.id }
        assertEquals(3, channels.size)
        assertEquals(NotificationManager.IMPORTANCE_DEFAULT, channels.getValue(NotificationChannels.STREAK).importance)
        assertEquals(NotificationManager.IMPORTANCE_LOW, channels.getValue(NotificationChannels.INACTIVITY).importance)
        assertEquals(
            NotificationManager.IMPORTANCE_HIGH,
            channels.getValue(NotificationChannels.ACHIEVEMENTS).importance,
        )
    }

    @Test
    @Config(qualifiers = "sk")
    fun `channel names are localized`() {
        NotificationChannels.create(context)

        assertEquals("Pripomienky série", manager.getNotificationChannel(NotificationChannels.STREAK).name)
        assertEquals("Úspechy", manager.getNotificationChannel(NotificationChannels.ACHIEVEMENTS).name)
    }
}
