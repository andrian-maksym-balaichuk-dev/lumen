package sk.mpage.lumen.core.designsystem.layout

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.IntrinsicMeasurable
import androidx.compose.ui.layout.IntrinsicMeasureScope
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.Measurable
import androidx.compose.ui.layout.MeasurePolicy
import androidx.compose.ui.layout.MeasureResult
import androidx.compose.ui.layout.MeasureScope
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
public fun JustifiedFlowRow(
    horizontalGap: Dp,
    modifier: Modifier = Modifier,
    verticalGap: Dp = 0.dp,
    content: @Composable () -> Unit,
) {
    val policy = remember(horizontalGap, verticalGap) { JustifiedFlowRowPolicy(horizontalGap, verticalGap) }
    Layout(content, modifier, policy)
}

private class JustifiedFlowRowPolicy(
    private val horizontalGap: Dp,
    private val verticalGap: Dp,
) : MeasurePolicy {
    override fun MeasureScope.measure(
        measurables: List<Measurable>,
        constraints: Constraints,
    ): MeasureResult {
        val gap = horizontalGap.roundToPx()
        val rowGap = verticalGap.roundToPx()
        val bounded = constraints.hasBoundedWidth
        val maxWidth = if (bounded) constraints.maxWidth else Int.MAX_VALUE
        val natural = measurables.map { naturalWidth(it, constraints.maxHeight).coerceAtMost(maxWidth) }
        val rows = rows(natural, gap, maxWidth)

        val placeables =
            rows.mapIndexed { rowIndex, row ->
                val stretch = bounded && rowIndex < rows.lastIndex
                val extra = if (stretch) maxWidth - row.sumOf { natural[it] } - gap * (row.count() - 1) else 0
                row.mapIndexed { position, index ->
                    val share = extra / row.count() + if (position < extra % row.count()) 1 else 0
                    measurables[index].measure(Constraints.fixedWidth(natural[index] + share))
                }
            }
        val heights = placeables.map { row -> row.maxOfOrNull { it.height } ?: 0 }
        val contentWidth = placeables.maxOfOrNull { row -> row.sumOf { it.width } + gap * (row.size - 1) } ?: 0
        val width = if (bounded) constraints.maxWidth else contentWidth
        val height = heights.sum() + rowGap * (heights.size - 1).coerceAtLeast(0)

        return layout(width, height.coerceIn(constraints.minHeight, constraints.maxHeight)) {
            var y = 0
            placeables.forEachIndexed { rowIndex, row ->
                var x = 0
                row.forEach { placeable ->
                    placeable.placeRelative(x, y + (heights[rowIndex] - placeable.height) / 2)
                    x += placeable.width + gap
                }
                y += heights[rowIndex] + rowGap
            }
        }
    }

    override fun IntrinsicMeasureScope.minIntrinsicWidth(
        measurables: List<IntrinsicMeasurable>,
        height: Int,
    ): Int = measurables.maxOfOrNull { it.maxIntrinsicWidth(height) } ?: 0

    override fun IntrinsicMeasureScope.maxIntrinsicWidth(
        measurables: List<IntrinsicMeasurable>,
        height: Int,
    ): Int =
        measurables.sumOf { it.maxIntrinsicWidth(height) } +
            horizontalGap.roundToPx() * (measurables.size - 1).coerceAtLeast(0)

    override fun IntrinsicMeasureScope.minIntrinsicHeight(
        measurables: List<IntrinsicMeasurable>,
        width: Int,
    ): Int = intrinsicHeight(measurables, width)

    override fun IntrinsicMeasureScope.maxIntrinsicHeight(
        measurables: List<IntrinsicMeasurable>,
        width: Int,
    ): Int = intrinsicHeight(measurables, width)

    private fun IntrinsicMeasureScope.intrinsicHeight(
        measurables: List<IntrinsicMeasurable>,
        width: Int,
    ): Int {
        val maxWidth = if (width == Constraints.Infinity) Int.MAX_VALUE else width
        val widths = measurables.map { it.maxIntrinsicWidth(Constraints.Infinity).coerceAtMost(maxWidth) }
        val rows = rows(widths, horizontalGap.roundToPx(), maxWidth)
        val heights = rows.map { row -> row.maxOfOrNull { measurables[it].maxIntrinsicHeight(widths[it]) } ?: 0 }
        return heights.sum() + verticalGap.roundToPx() * (heights.size - 1).coerceAtLeast(0)
    }
}

private fun naturalWidth(
    measurable: Measurable,
    maxHeight: Int,
): Int = measurable.maxIntrinsicWidth(if (maxHeight == Constraints.Infinity) Constraints.Infinity else maxHeight)

private fun rows(
    widths: List<Int>,
    gap: Int,
    maxWidth: Int,
): List<IntRange> {
    val rows = mutableListOf<IntRange>()
    var start = 0
    var used = 0
    widths.forEachIndexed { index, width ->
        val needed = if (index == start) width else used + gap + width
        if (index > start && needed > maxWidth) {
            rows += start until index
            start = index
            used = width
        } else {
            used = needed
        }
    }
    if (widths.isNotEmpty()) rows += start until widths.size
    return rows
}
