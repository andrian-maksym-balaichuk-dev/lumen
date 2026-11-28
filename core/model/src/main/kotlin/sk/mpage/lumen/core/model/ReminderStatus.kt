package sk.mpage.lumen.core.model

import java.time.Instant

public data class ReminderStatus(
    val nextCheckAt: Instant? = null,
    val manualCheck: ManualCheck = ManualCheck.IDLE,
)

public enum class ManualCheck { IDLE, QUEUED, RUNNING, POSTED, NOTHING_TO_REMIND, NOT_ALLOWED }
