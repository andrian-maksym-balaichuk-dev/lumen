package sk.mpage.lumen.core.designsystem.tokens

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Immutable
public data class LumoraSpacing(
    val xxs: Dp = 2.dp,
    val xs: Dp = 4.dp,
    val sm: Dp = 8.dp,
    val md: Dp = 12.dp,
    val lg: Dp = 16.dp,
    val xl: Dp = 20.dp,
    val xxl: Dp = 24.dp,
    val xxxl: Dp = 32.dp,
    val huge: Dp = 48.dp,
    val gutterCompact: Dp = 20.dp,
    val gutterMedium: Dp = 24.dp,
    val contentMaxWidth: Dp = 640.dp,
    val cardPadding: Dp = 16.dp,
    val listGap: Dp = 12.dp,
    val sectionGap: Dp = 32.dp,
    val minTouchTarget: Dp = 48.dp,
)

internal val LocalSpacing = staticCompositionLocalOf { LumoraSpacing() }
