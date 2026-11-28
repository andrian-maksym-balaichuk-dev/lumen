package sk.mpage.lumen.core.testing.port

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import sk.mpage.lumen.core.domain.port.ReminderCheck
import sk.mpage.lumen.core.model.ManualCheck
import sk.mpage.lumen.core.model.ReminderStatus

public class FakeReminderCheck : ReminderCheck {
    private val state = MutableStateFlow(ReminderStatus())
    private val run = MutableSharedFlow<ManualCheck>(replay = 8)

    public var runs: Int = 0
        private set

    override val status: Flow<ReminderStatus> = state

    override fun runNow(): Flow<ManualCheck> {
        runs++
        return run
    }

    public fun emit(status: ReminderStatus) {
        state.value = status
    }

    public fun finishRun(check: ManualCheck) {
        run.tryEmit(check)
    }
}
