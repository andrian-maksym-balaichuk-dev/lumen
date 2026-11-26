package sk.mpage.lumen.core.testing.port

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import sk.mpage.lumen.core.domain.port.NetworkMonitor

public class FakeNetworkMonitor(
    online: Boolean = true,
) : NetworkMonitor {
    private val state = MutableStateFlow(online)

    override val isOnline: SharedFlow<Boolean> = state

    public fun setOnline(online: Boolean) {
        state.value = online
    }
}
