package sk.mpage.lumen.core.designsystem.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.tooling.preview.PreviewLightDark
import sk.mpage.lumen.core.designsystem.R
import sk.mpage.lumen.core.designsystem.icon.LumoraIcons
import sk.mpage.lumen.core.designsystem.modifier.shimmer
import sk.mpage.lumen.core.designsystem.preview.PreviewSurface
import sk.mpage.lumen.core.designsystem.theme.Lumora

@Composable
public fun <T> CategoryChipGrid(
    options: List<T>,
    selected: T,
    onSelect: (T) -> Unit,
    label: (T) -> String,
    icon: (T) -> Int,
    modifier: Modifier = Modifier,
) {
    FlowRow(
        modifier = modifier.fillMaxWidth().selectableGroup(),
        horizontalArrangement = Arrangement.spacedBy(Lumora.spacing.sm),
    ) {
        options.forEach { option ->
            FilterChip(
                selected = option == selected,
                onClick = { onSelect(option) },
                label = { Text(label(option)) },
                leadingIcon = {
                    Icon(
                        painter = painterResource(icon(option)),
                        contentDescription = null,
                        modifier = Modifier.size(FilterChipDefaults.IconSize),
                    )
                },
                colors = FilterChipDefaults.filterChipColors(containerColor = Lumora.glass.frost.tint),
            )
        }
    }
}

@Composable
public fun CategoryChipPlaceholders(modifier: Modifier = Modifier) {
    val sizes = Lumora.sizes
    val widths = listOf(sizes.xxxl, sizes.huge, sizes.xl, sizes.huge, sizes.xxl, sizes.xxxl, sizes.xl, sizes.huge)
    val description = stringResource(R.string.category_chips_loading)
    val base = MaterialTheme.colorScheme.surfaceContainerHighest
    val highlight = MaterialTheme.colorScheme.surfaceBright
    val animate = !Lumora.motion.isReduced
    FlowRow(
        modifier = modifier.fillMaxWidth().clearAndSetSemantics { contentDescription = description },
        horizontalArrangement = Arrangement.spacedBy(Lumora.spacing.sm),
        verticalArrangement = Arrangement.spacedBy(Lumora.spacing.sm),
    ) {
        widths.forEach { width ->
            Spacer(
                Modifier
                    .size(width, FilterChipDefaults.Height)
                    .clip(MaterialTheme.shapes.small)
                    .shimmer(base, highlight, animate),
            )
        }
    }
}

@PreviewLightDark
@Composable
private fun CategoryChipGridPreview() {
    PreviewSurface {
        var selected by remember { mutableStateOf("Any") }
        CategoryChipGrid(
            options = listOf("Any", "General Knowledge", "Books", "Film", "Music", "Science & Nature", "Animals"),
            selected = selected,
            onSelect = { selected = it },
            label = { it },
            icon = { if (it == "Any") LumoraIcons.Shuffle else LumoraIcons.Category },
            modifier = Modifier.padding(Lumora.spacing.lg),
        )
    }
}

@PreviewLightDark
@Composable
private fun CategoryChipPlaceholdersPreview() {
    PreviewSurface {
        CategoryChipPlaceholders(Modifier.padding(Lumora.spacing.lg))
    }
}
