package sk.mpage.lumen.core.ui

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.provider.Settings
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LifecycleResumeEffect

@Stable
public class NotificationPermissionState internal constructor(
    private val context: Context,
) {
    public var isGranted: Boolean by mutableStateOf(check(context))
        private set

    public var isBlocked: Boolean by mutableStateOf(false)
        internal set

    internal var launch: () -> Unit = {}
    internal var onResult: ((Boolean) -> Unit)? = null

    public fun request(onResult: (Boolean) -> Unit) {
        if (refresh()) {
            onResult(true)
            return
        }
        this.onResult = onResult
        launch()
    }

    public fun openSettings() {
        context.startActivity(
            Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
                .putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
        )
    }

    internal fun refresh(): Boolean {
        isGranted = check(context)
        if (isGranted) isBlocked = false
        return isGranted
    }

    private companion object {
        fun check(context: Context): Boolean =
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) ==
                PackageManager.PERMISSION_GRANTED &&
                NotificationManagerCompat.from(context).areNotificationsEnabled()
    }
}

@Composable
public fun rememberNotificationPermissionState(): NotificationPermissionState {
    val context = LocalContext.current
    val activity = LocalActivity.current
    var blocked by rememberSaveable { mutableStateOf(false) }
    val state = remember(context) { NotificationPermissionState(context) }
    val launcher =
        rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            state.refresh()
            val canAskAgain =
                activity?.shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS) == true
            blocked = !state.isGranted && (granted || !canAskAgain)
            state.isBlocked = blocked
            state.onResult?.invoke(state.isGranted)
            state.onResult = null
        }
    state.launch = { launcher.launch(Manifest.permission.POST_NOTIFICATIONS) }
    state.isBlocked = blocked && !state.isGranted
    LifecycleResumeEffect(state) {
        state.refresh()
        onPauseOrDispose {}
    }
    return state
}
