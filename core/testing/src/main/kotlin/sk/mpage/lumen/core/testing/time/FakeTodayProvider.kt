package sk.mpage.lumen.core.testing.time

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import sk.mpage.lumen.core.domain.port.TodayProvider
import java.time.LocalDate

public class FakeTodayProvider(
    initial: LocalDate,
) : TodayProvider {
    private val state = MutableStateFlow(initial)

    override val today: SharedFlow<LocalDate> = state

    public fun setToday(date: LocalDate) {
        state.value = date
    }
}
