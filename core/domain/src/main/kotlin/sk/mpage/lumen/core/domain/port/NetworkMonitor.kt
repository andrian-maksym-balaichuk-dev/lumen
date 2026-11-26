package sk.mpage.lumen.core.domain.port

import kotlinx.coroutines.flow.SharedFlow

public interface NetworkMonitor {
    public val isOnline: SharedFlow<Boolean>
}
