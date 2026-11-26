package sk.mpage.lumen.core.designsystem.modifier

import androidx.compose.ui.Modifier
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeSource

public fun Modifier.hazeBackground(state: HazeState): Modifier = hazeSource(state, zIndex = BACKGROUND_Z)

public fun Modifier.hazeContent(state: HazeState?): Modifier =
    if (state == null) this else hazeSource(state, zIndex = CONTENT_Z)

private const val BACKGROUND_Z = 0f
private const val CONTENT_Z = 1f
