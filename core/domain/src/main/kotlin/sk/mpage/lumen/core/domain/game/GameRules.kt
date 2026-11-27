package sk.mpage.lumen.core.domain.game

import sk.mpage.lumen.core.model.GameMode

public object GameRules {
    public const val CLASSIC_QUESTION_COUNT: Int = 10
    public const val QUESTION_TIME_LIMIT_MS: Long = 15_000L
    public const val REVEAL_DURATION_MS: Long = 1_200L
    public const val QUALIFYING_PERCENT: Int = 80

    public fun isNewMarathonBest(
        mode: GameMode,
        correct: Int,
        previousBest: Int?,
    ): Boolean = mode == GameMode.MARATHON && correct > (previousBest ?: 0)
}
