package sk.mpage.lumen.core.designsystem.layout

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import sk.mpage.lumen.core.designsystem.preview.PreviewSurface
import sk.mpage.lumen.core.designsystem.theme.Lumora

@Composable
public fun LumoraFlex(
    items: List<@Composable () -> Unit>,
    modifier: Modifier = Modifier,
    minItemWidth: Dp = LumoraWindow.PaneMinWidth,
    maxItemWidth: Dp = Lumora.spacing.contentMaxWidth,
    columnGap: Dp = Lumora.spacing.xxxl,
    rowGap: Dp = Lumora.spacing.xxl,
    contentAlignment: Alignment = Alignment.TopCenter,
    itemAlignment: Alignment.Vertical = Alignment.Top,
) {
    LumoraFlex(
        modifier = modifier,
        minItemWidth = minItemWidth,
        maxItemWidth = maxItemWidth,
        columnGap = columnGap,
        rowGap = rowGap,
        contentAlignment = contentAlignment,
        itemAlignment = itemAlignment,
        itemsFor = { items },
    )
}

@Composable
public fun LumoraFlex(
    itemsFor: (columns: Int) -> List<@Composable () -> Unit>,
    modifier: Modifier = Modifier,
    minItemWidth: Dp = LumoraWindow.PaneMinWidth,
    maxItemWidth: Dp = Lumora.spacing.contentMaxWidth,
    columnGap: Dp = Lumora.spacing.xxxl,
    rowGap: Dp = Lumora.spacing.xxl,
    contentAlignment: Alignment = Alignment.TopCenter,
    itemAlignment: Alignment.Vertical = Alignment.Top,
) {
    BoxWithConstraints(modifier, contentAlignment = contentAlignment) {
        val fit = ((maxWidth + columnGap) / (minItemWidth + columnGap)).toInt().coerceAtLeast(1)
        val items = itemsFor(fit)
        val columns = fit.coerceAtMost(items.size.coerceAtLeast(1))
        FlowRow(
            modifier =
                Modifier
                    .widthIn(max = maxItemWidth * columns + columnGap * (columns - 1))
                    .fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(columnGap),
            verticalArrangement = Arrangement.spacedBy(rowGap),
            itemVerticalAlignment = itemAlignment,
            maxItemsInEachRow = columns,
        ) {
            items.forEach { item -> Box(Modifier.weight(1f)) { item() } }
        }
    }
}

@Preview(widthDp = 914, heightDp = 411)
@Preview(widthDp = 700, heightDp = 411)
@Preview(widthDp = 411, heightDp = 914)
@Composable
private fun LumoraFlexPreview() {
    PreviewSurface {
        LumoraFlex(
            items =
                List(3) { index ->
                    {
                        Surface(
                            modifier = Modifier.fillMaxWidth().height(Lumora.sizes.xxxl),
                            color = MaterialTheme.colorScheme.primaryContainer,
                            shape = MaterialTheme.shapes.medium,
                        ) { Text("Item ${index + 1}", Modifier.padding(Lumora.spacing.lg)) }
                    }
                },
            modifier = Modifier.fillMaxSize().padding(Lumora.spacing.gutterCompact),
        )
    }
}
