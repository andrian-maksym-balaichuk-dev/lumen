package sk.mpage.lumen.core.designsystem.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.style.TextOverflow
import sk.mpage.lumen.core.designsystem.layout.LumoraWindow
import sk.mpage.lumen.core.designsystem.preview.LumoraPreviews
import sk.mpage.lumen.core.designsystem.preview.PreviewSurface
import sk.mpage.lumen.core.designsystem.theme.Lumora

@OptIn(ExperimentalMaterial3Api::class)
@Composable
public fun LumoraLargeTopBar(
    title: String,
    scrollBehavior: TopAppBarScrollBehavior,
    modifier: Modifier = Modifier,
    navigationIcon: @Composable () -> Unit = {},
    actions: @Composable RowScope.() -> Unit = {},
) {
    val colors =
        TopAppBarDefaults.topAppBarColors(
            containerColor = Color.Transparent,
            scrolledContainerColor = Color.Transparent,
            titleContentColor = MaterialTheme.colorScheme.onBackground,
        )
    val titleText = @Composable { Text(text = title, maxLines = 1, overflow = TextOverflow.Ellipsis) }
    Box(modifier) {
        GlassSurface(
            modifier =
                Modifier
                    .matchParentSize()
                    .graphicsLayer {
                        val state = scrollBehavior.state
                        alpha = maxOf(state.collapsedFraction, state.overlappedFraction)
                    },
            material = if (LocalHazeState.current != null) Lumora.glass.thin else Lumora.glass.thick,
            shape = RectangleShape,
        ) {}
        if (LumoraWindow.isCompactHeight) {
            TopAppBar(
                title = titleText,
                navigationIcon = navigationIcon,
                actions = actions,
                colors = colors,
                scrollBehavior = scrollBehavior,
            )
        } else {
            LargeTopAppBar(
                title = titleText,
                navigationIcon = navigationIcon,
                actions = actions,
                colors = colors,
                scrollBehavior = scrollBehavior,
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@LumoraPreviews
@Composable
private fun LumoraLargeTopBarPreview() {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    PreviewSurface {
        Scaffold(
            modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
            containerColor = Color.Transparent,
            topBar = { LumoraLargeTopBar(title = "Good evening, Ada", scrollBehavior = scrollBehavior) },
        ) { padding ->
            Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(padding)) {
                repeat(30) { Text("Row ${it + 1}", Modifier.padding(Lumora.spacing.lg)) }
            }
        }
    }
}
