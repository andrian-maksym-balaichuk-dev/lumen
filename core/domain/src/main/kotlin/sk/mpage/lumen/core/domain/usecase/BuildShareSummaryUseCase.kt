package sk.mpage.lumen.core.domain.usecase

import sk.mpage.lumen.core.model.GameMode
import sk.mpage.lumen.core.model.QuizSession
import sk.mpage.lumen.core.model.ShareSummary
import javax.inject.Inject

public class BuildShareSummaryUseCase
    @Inject
    constructor() {
        public operator fun invoke(
            session: QuizSession,
            isNewBest: Boolean,
        ): ShareSummary =
            ShareSummary(
                mode = session.config.mode,
                correct = session.correctCount,
                total = session.totalQuestions,
                percent = session.scorePercent,
                categoryName = session.config.categoryName,
                difficulty = session.config.difficulty,
                durationSeconds = session.durationSeconds,
                isNewBest = isNewBest && session.config.mode == GameMode.MARATHON,
            )
    }
