package sk.mpage.lumen

import android.os.StrictMode

internal fun installStrictMode() {
    StrictMode.setThreadPolicy(
        StrictMode.ThreadPolicy
            .Builder()
            .detectAll()
            .penaltyLog()
            .build(),
    )
    StrictMode.setVmPolicy(
        StrictMode.VmPolicy
            .Builder()
            .detectAll()
            .penaltyLog()
            .build(),
    )
}
