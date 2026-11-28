package sk.mpage.lumen.core.domain.usecase

import sk.mpage.lumen.core.common.result.AppResult
import sk.mpage.lumen.core.common.result.map
import sk.mpage.lumen.core.domain.game.GameRules
import sk.mpage.lumen.core.domain.port.AchievementNotifier
import sk.mpage.lumen.core.domain.repository.QuizHistoryRepository
import sk.mpage.lumen.core.model.FinishedGame
import javax.inject.Inject

public class SaveQuizResultUseCase
    @Inject
    constructor(
        private val history: QuizHistoryRepository,
        private val achievements: AchievementNotifier,
    ) {
        public suspend operator fun invoke(game: FinishedGame): AppResult<SaveResult> =
            history.save(game).map { outcome ->
                val previousBest = outcome.previousMarathonBest
                val isNewBest =
                    !outcome.alreadySaved &&
                        GameRules.isNewMarathonBest(game.config.mode, game.correctCount, previousBest)
                if (isNewBest && previousBest != null) {
                    achievements.showMarathonRecord(outcome.sessionId, game.correctCount, previousBest)
                }
                SaveResult(sessionId = outcome.sessionId, isNewBest = isNewBest, previousBest = previousBest)
            }
    }

public data class SaveResult(
    val sessionId: Long,
    val isNewBest: Boolean,
    val previousBest: Int?,
)
