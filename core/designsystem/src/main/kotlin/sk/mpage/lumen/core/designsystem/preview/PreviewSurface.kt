package sk.mpage.lumen.core.designsystem.preview

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import sk.mpage.lumen.core.designsystem.component.AuraBackground
import sk.mpage.lumen.core.designsystem.theme.LumoraTheme

@Composable
public fun PreviewSurface(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    LumoraTheme(darkTheme = darkTheme) {
        Box {
            AuraBackground(Modifier.matchParentSize())
            content()
        }
    }
}
