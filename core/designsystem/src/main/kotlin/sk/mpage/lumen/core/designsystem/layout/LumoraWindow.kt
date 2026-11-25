package sk.mpage.lumen.core.designsystem.layout

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

public object LumoraWindow {
    public val CompactHeight: Dp = 480.dp
    public val PaneMinWidth: Dp = 280.dp

    public val isCompactHeight: Boolean
        @Composable
        @ReadOnlyComposable
        get() =
            with(LocalDensity.current) {
                LocalWindowInfo.current.containerSize.height
                    .toDp()
            } < CompactHeight
}
