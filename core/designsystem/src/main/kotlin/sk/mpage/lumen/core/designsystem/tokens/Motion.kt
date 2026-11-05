package sk.mpage.lumen.core.designsystem.tokens

import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf

@Immutable
public class LumoraMotion private constructor(
    public val isReduced: Boolean,
) {
    public fun <T> press(): FiniteAnimationSpec<T> = springOrSnap(0.7f, 1400f)

    public fun <T> selection(): FiniteAnimationSpec<T> = springOrSnap(0.8f, 800f)

    public fun <T> dockResize(): FiniteAnimationSpec<T> = springOrSnap(0.85f, 500f)

    public fun <T> screen(): FiniteAnimationSpec<T> = springOrSnap(0.8f, 380f)

    public fun <T> questionChange(): FiniteAnimationSpec<T> = springOrSnap(0.85f, 500f)

    public fun <T> optionEnter(): FiniteAnimationSpec<T> = springOrSnap(0.75f, 600f)

    public fun <T> correctPop(): FiniteAnimationSpec<T> = springOrSnap(0.45f, 600f)

    public fun <T> badgePop(): FiniteAnimationSpec<T> = springOrSnap(0.4f, 500f)

    public fun <T> streakDot(): FiniteAnimationSpec<T> = springOrSnap(0.5f, 700f)

    public fun <T> colorChange(): FiniteAnimationSpec<T> = if (isReduced) tween(REDUCED_FADE_MS) else spring(1f, 1600f)

    public val optionStaggerMs: Int get() = if (isReduced) 0 else 40

    public val streakDotStaggerMs: Int get() = if (isReduced) 0 else 60

    private fun <T> springOrSnap(
        dampingRatio: Float,
        stiffness: Float,
    ): FiniteAnimationSpec<T> = if (isReduced) snap() else spring(dampingRatio, stiffness)

    public companion object {
        private const val REDUCED_FADE_MS = 150

        public val Default: LumoraMotion = LumoraMotion(isReduced = false)
        public val Reduced: LumoraMotion = LumoraMotion(isReduced = true)
    }
}

internal val LocalMotion = staticCompositionLocalOf { LumoraMotion.Default }
