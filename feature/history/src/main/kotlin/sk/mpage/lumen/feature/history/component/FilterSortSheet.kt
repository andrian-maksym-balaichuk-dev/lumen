package sk.mpage.lumen.feature.history.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import sk.mpage.lumen.core.designsystem.component.CategoryChipGrid
import sk.mpage.lumen.core.designsystem.component.LumoraSegmented
import sk.mpage.lumen.core.designsystem.theme.Lumora
import sk.mpage.lumen.core.model.Category
import sk.mpage.lumen.core.model.GameMode
import sk.mpage.lumen.core.model.HistoryFilter
import sk.mpage.lumen.core.model.HistorySort
import sk.mpage.lumen.core.ui.categoryIcon
import sk.mpage.lumen.core.ui.displayName
import sk.mpage.lumen.feature.history.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun FilterSortSheet(
    filter: HistoryFilter,
    categories: List<Category>,
    onSortChanged: (HistorySort) -> Unit,
    onCategoryChanged: (Int?) -> Unit,
    onModeChanged: (GameMode?) -> Unit,
    onReset: () -> Unit,
    onDismiss: () -> Unit,
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        shape = MaterialTheme.shapes.large,
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
    ) {
        Column(
            modifier = Modifier.padding(horizontal = Lumora.spacing.gutterCompact).padding(bottom = Lumora.spacing.xxl),
            verticalArrangement = Arrangement.spacedBy(Lumora.spacing.xxl),
        ) {
            SheetSection(
                title = stringResource(R.string.history_sort),
                supportingText =
                    stringResource(R.string.history_sort_fastest_info).takeIf {
                        filter.sort ==
                            HistorySort.FASTEST
                    },
            ) {
                LumoraSegmented(
                    options = HistorySort.entries,
                    selected = filter.sort,
                    onSelect = onSortChanged,
                    label = { sortLabel(it) },
                )
            }
            if (categories.isNotEmpty()) {
                SheetSection(stringResource(R.string.history_filter_category)) {
                    val resources = LocalResources.current
                    val anyLabel = stringResource(R.string.history_any_category)
                    val names = categories.associate { it.id to it.displayName(resources) }
                    CategoryChipGrid(
                        options = listOf(null) + categories.map { it.id },
                        selected = filter.categoryId,
                        onSelect = onCategoryChanged,
                        label = { id -> if (id == null) anyLabel else names.getValue(id) },
                        icon = ::categoryIcon,
                    )
                }
            }
            SheetSection(stringResource(R.string.history_filter_mode)) {
                ModeSelector(filter.mode, onModeChanged)
            }
            TextButton(onClick = onReset, modifier = Modifier.align(Alignment.End)) {
                Text(stringResource(R.string.history_reset))
            }
        }
    }
}

@Composable
internal fun ModeSelector(
    mode: GameMode?,
    onModeChanged: (GameMode?) -> Unit,
    modifier: Modifier = Modifier,
) {
    LumoraSegmented(
        options = listOf(null, GameMode.CLASSIC, GameMode.MARATHON),
        selected = mode,
        onSelect = onModeChanged,
        label = {
            stringResource(
                when (it) {
                    null -> R.string.history_mode_all
                    GameMode.CLASSIC -> R.string.history_mode_classic
                    GameMode.MARATHON -> R.string.history_mode_marathon
                },
            )
        },
        modifier = modifier,
    )
}

@Composable
private fun SheetSection(
    title: String,
    supportingText: String? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(Lumora.spacing.sm)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.semantics { heading() },
        )
        content()
        if (supportingText != null) {
            Text(
                text = supportingText,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun sortLabel(sort: HistorySort): String =
    stringResource(
        when (sort) {
            HistorySort.NEWEST -> R.string.history_sort_newest
            HistorySort.BEST_SCORE -> R.string.history_sort_best
            HistorySort.FASTEST -> R.string.history_sort_fastest
        },
    )
