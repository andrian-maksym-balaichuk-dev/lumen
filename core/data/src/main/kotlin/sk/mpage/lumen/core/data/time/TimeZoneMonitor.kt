package sk.mpage.lumen.core.data.time

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.core.content.ContextCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.conflate
import java.time.ZoneId
import javax.inject.Inject

internal interface TimeZoneMonitor {
    val zones: Flow<ZoneId>
}

internal class BroadcastTimeZoneMonitor
    @Inject
    constructor(
        @ApplicationContext private val context: Context,
    ) : TimeZoneMonitor {
        override val zones: Flow<ZoneId> =
            callbackFlow {
                val receiver =
                    object : BroadcastReceiver() {
                        override fun onReceive(
                            context: Context,
                            intent: Intent,
                        ) {
                            val zone =
                                intent
                                    .getStringExtra(Intent.EXTRA_TIMEZONE)
                                    ?.let { runCatching { ZoneId.of(it) }.getOrNull() }
                            trySend(zone ?: ZoneId.systemDefault())
                        }
                    }
                val filter =
                    IntentFilter().apply {
                        addAction(Intent.ACTION_TIMEZONE_CHANGED)
                        addAction(Intent.ACTION_TIME_CHANGED)
                    }
                trySend(ZoneId.systemDefault())
                ContextCompat.registerReceiver(context, receiver, filter, ContextCompat.RECEIVER_NOT_EXPORTED)
                awaitClose { context.unregisterReceiver(receiver) }
            }.conflate()
    }
