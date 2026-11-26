package sk.mpage.lumen.core.designsystem.tokens

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Immutable
public data class LumoraSizes(
    val xxs: Dp = 16.dp,
    val xs: Dp = 24.dp,
    val sm: Dp = 36.dp,
    val md: Dp = 48.dp,
    val lg: Dp = 56.dp,
    val xl: Dp = 64.dp,
    val xxl: Dp = 72.dp,
    val xxxl: Dp = 96.dp,
    val huge: Dp = 112.dp,
    val giant: Dp = 320.dp,
)

internal val LocalSizes = staticCompositionLocalOf { LumoraSizes() }
