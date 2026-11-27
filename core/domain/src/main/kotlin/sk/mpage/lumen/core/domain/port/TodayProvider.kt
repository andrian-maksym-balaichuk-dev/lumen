package sk.mpage.lumen.core.domain.port

import kotlinx.coroutines.flow.SharedFlow
import java.time.LocalDate

public interface TodayProvider {
    public val today: SharedFlow<LocalDate>
}
