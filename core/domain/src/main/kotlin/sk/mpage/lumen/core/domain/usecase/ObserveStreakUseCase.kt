package sk.mpage.lumen.core.domain.usecase

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import sk.mpage.lumen.core.domain.port.TodayProvider
import sk.mpage.lumen.core.domain.repository.QuizHistoryRepository
import sk.mpage.lumen.core.model.StreakState
import javax.inject.Inject

public class ObserveStreakUseCase
    @Inject
    constructor(
        private val history: QuizHistoryRepository,
        private val todayProvider: TodayProvider,
        private val calculate: CalculateStreakUseCase,
    ) {
        public operator fun invoke(): Flow<StreakState> =
            combine(history.observeQualifyingDays(), todayProvider.today, calculate::invoke).distinctUntilChanged()
    }
