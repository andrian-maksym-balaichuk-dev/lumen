package sk.mpage.lumen.core.designsystem.component

import androidx.annotation.DrawableRes
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import sk.mpage.lumen.core.designsystem.icon.LumoraIcons
import sk.mpage.lumen.core.designsystem.preview.LumoraPreviews
import sk.mpage.lumen.core.designsystem.preview.PreviewSurface
import sk.mpage.lumen.core.designsystem.theme.Lumora

@Immutable
public data class LumoraDockItem(
    val label: String,
    @param:DrawableRes val iconRes: Int,
)

/** Floating navigation chrome. Selection and navigation remain owned by the caller. */
@Composable
public fun LumoraDock(
    items: List<LumoraDockItem>,
    selectedIndex: Int,
    onItemSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    require(items.size in 3..5) { "A dock needs three to five destinations" }
    require(selectedIndex in items.indices) { "The selected destination must belong to the dock" }

    val spacing = Lumora.spacing
    val showLabels = LocalDensity.current.fontScale <= 1.5f
    val selectedPosition by animateFloatAsState(
        targetValue = selectedIndex.toFloat(),
        animationSpec = Lumora.motion.selection(),
        label = "Dock selection",
    )

    Box(
        modifier =
            modifier
                .windowInsetsPadding(
                    WindowInsets.navigationBars.only(WindowInsetsSides.Horizontal + WindowInsetsSides.Bottom),
                ).padding(horizontal = spacing.gutterCompact)
                .padding(bottom = spacing.lg),
        contentAlignment = Alignment.Center,
    ) {
        GlassSurface(
            modifier = Modifier.widthIn(max = spacing.contentMaxWidth).fillMaxWidth(),
        ) {
            BoxWithConstraints(Modifier.padding(DockPadding).height(DockItemHeight)) {
                // NavigationBar places an 8 dp gap between its equally weighted items.
                val itemWidth = (maxWidth - spacing.sm * (items.size - 1)) / items.size
                Box(
                    Modifier
                        .offset {
                            IntOffset(x = ((itemWidth + spacing.sm) * selectedPosition).roundToPx(), y = 0)
                        }.width(itemWidth)
                        .height(DockItemHeight)
                        .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
                )
                NavigationBar(
                    modifier = Modifier.height(DockItemHeight),
                    containerColor = Color.Transparent,
                    tonalElevation = 0.dp,
                    windowInsets = WindowInsets(0),
                ) {
                    items.forEachIndexed { index, item ->
                        val selected = index == selectedIndex
                        val color by animateColorAsState(
                            targetValue =
                                if (selected) {
                                    MaterialTheme.colorScheme.onPrimaryContainer
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                },
                            animationSpec = Lumora.motion.colorChange(),
                            label = "Dock item color",
                        )
                        NavigationBarItem(
                            selected = selected,
                            onClick = { onItemSelected(index) },
                            modifier =
                                Modifier
                                    .height(DockItemHeight)
                                    .clip(CircleShape),
                            colors = NavigationBarItemDefaults.colors(indicatorColor = Color.Transparent),
                            icon = {
                                Column(
                                    modifier = Modifier.clearAndSetSemantics { contentDescription = item.label },
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(spacing.xxs),
                                ) {
                                    Icon(
                                        painter = painterResource(item.iconRes),
                                        contentDescription = null,
                                        modifier = Modifier.size(24.dp),
                                        tint = color,
                                    )
                                    if (showLabels) {
                                        Text(
                                            text = item.label,
                                            color = color,
                                            style = MaterialTheme.typography.labelSmall,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                        )
                                    }
                                }
                            },
                        )
                    }
                }
            }
        }
    }
}

private val DockPadding = 6.dp
private val DockItemHeight = 52.dp

@LumoraPreviews
@Composable
private fun LumoraDockPreview() {
    var selectedIndex by remember { mutableIntStateOf(0) }
    PreviewSurface {
        Box(Modifier.fillMaxSize()) {
            LumoraDock(
                items =
                    listOf(
                        LumoraDockItem("Play", LumoraIcons.Play),
                        LumoraDockItem("History", LumoraIcons.History),
                        LumoraDockItem("Awards", LumoraIcons.Awards),
                        LumoraDockItem("Profile", LumoraIcons.Profile),
                    ),
                selectedIndex = selectedIndex,
                onItemSelected = { selectedIndex = it },
                modifier = Modifier.align(Alignment.BottomCenter),
            )
        }
    }
}
