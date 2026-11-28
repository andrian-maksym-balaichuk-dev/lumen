package sk.mpage.lumen.core.domain.port

import kotlinx.coroutines.flow.Flow
import sk.mpage.lumen.core.model.ManualCheck
import sk.mpage.lumen.core.model.ReminderStatus

public interface ReminderCheck {
    public val status: Flow<ReminderStatus>

    public fun runNow(): Flow<ManualCheck>
}
