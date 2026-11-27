package sk.mpage.lumen.core.domain.usecase

import sk.mpage.lumen.core.common.result.AppResult
import sk.mpage.lumen.core.common.result.map
import sk.mpage.lumen.core.domain.game.GameRules
import sk.mpage.lumen.core.domain.repository.QuizHistoryRepository
import sk.mpage.lumen.core.model.FinishedGame
import javax.inject.Inject

public class SaveQuizResultUseCase
    @Inject
    constructor(
        private val history: QuizHistoryRepository,
    ) {
        public suspend operator fun invoke(game: FinishedGame): AppResult<SaveResult> =
            history.save(game).map { outcome ->
                SaveResult(
                    sessionId = outcome.sessionId,
                    isNewBest =
                        !outcome.alreadySaved &&
                            GameRules.isNewMarathonBest(
                                game.config.mode,
                                game.correctCount,
                                outcome.previousMarathonBest,
                            ),
                    previousBest = outcome.previousMarathonBest,
                )
            }
    }

public data class SaveResult(
    val sessionId: Long,
    val isNewBest: Boolean,
    val previousBest: Int?,
)
