package sk.mpage.lumen.core.designsystem.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import sk.mpage.lumen.core.designsystem.layout.LumoraWindow
import sk.mpage.lumen.core.designsystem.preview.PreviewSurface
import sk.mpage.lumen.core.designsystem.theme.Lumora

@Composable
public fun LumoraTwoPane(
    start: @Composable ColumnScope.() -> Unit,
    end: @Composable ColumnScope.() -> Unit,
    singlePane: @Composable BoxScope.() -> Unit,
    modifier: Modifier = Modifier,
    scrollPanes: Boolean = false,
    paneArrangement: Arrangement.Vertical = Arrangement.spacedBy(Lumora.spacing.xxl),
) {
    val spacing = Lumora.spacing
    val gap = spacing.xxxl
    val gutter = spacing.gutterCompact
    BoxWithConstraints(modifier) {
        if (maxWidth >= LumoraWindow.PaneMinWidth * 2 + gap + gutter * 2) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = gutter),
                horizontalArrangement = Arrangement.spacedBy(gap, Alignment.CenterHorizontally),
            ) {
                Pane(scrollPanes, paneArrangement, start)
                Pane(scrollPanes, paneArrangement, end)
            }
        } else {
            singlePane()
        }
    }
}

@Composable
private fun RowScope.Pane(
    scroll: Boolean,
    arrangement: Arrangement.Vertical,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier =
            Modifier
                .weight(1f)
                .widthIn(max = Lumora.spacing.contentMaxWidth)
                .then(if (scroll) Modifier.fillMaxHeight().verticalScroll(rememberScrollState()) else Modifier)
                .padding(vertical = Lumora.spacing.lg),
        verticalArrangement = arrangement,
        content = content,
    )
}

@Preview(widthDp = 914, heightDp = 411)
@Preview(widthDp = 411, heightDp = 914)
@Composable
private fun LumoraTwoPanePreview() {
    PreviewSurface {
        LumoraTwoPane(
            start = { Text("Start pane") },
            end = { Text("End pane") },
            singlePane = { Text("Single pane", Modifier.padding(Lumora.spacing.gutterCompact)) },
            modifier = Modifier.fillMaxSize(),
        )
    }
}
