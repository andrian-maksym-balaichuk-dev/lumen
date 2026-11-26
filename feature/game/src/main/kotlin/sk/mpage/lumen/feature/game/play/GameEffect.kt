package sk.mpage.lumen.feature.game.play

internal sealed interface GameEffect {
    data class ShowResults(
        val sessionId: Long,
    ) : GameEffect

    data object Closed : GameEffect
}
