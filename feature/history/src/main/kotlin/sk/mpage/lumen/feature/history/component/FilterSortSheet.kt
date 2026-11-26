package sk.mpage.lumen.feature.history.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import sk.mpage.lumen.core.designsystem.theme.Lumora
import sk.mpage.lumen.core.model.Category
import sk.mpage.lumen.core.model.GameMode
import sk.mpage.lumen.core.model.HistoryFilter
import sk.mpage.lumen.core.model.HistorySort
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
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier.padding(horizontal = Lumora.spacing.gutterCompact).padding(bottom = Lumora.spacing.xxl),
            verticalArrangement = Arrangement.spacedBy(Lumora.spacing.md),
        ) {
            SheetTitle(stringResource(R.string.history_sort))
            Column(Modifier.selectableGroup()) {
                HistorySort.entries.forEach { sort ->
                    Row(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .minimumInteractiveComponentSize()
                                .selectable(
                                    selected = sort == filter.sort,
                                    role = Role.RadioButton,
                                    onClick = { onSortChanged(sort) },
                                ),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        RadioButton(selected = sort == filter.sort, onClick = null)
                        Text(sortLabel(sort), modifier = Modifier.padding(start = Lumora.spacing.md))
                    }
                }
            }
            SheetTitle(stringResource(R.string.history_filter_category))
            val resources = LocalResources.current
            FlowRow(horizontalArrangement = Arrangement.spacedBy(Lumora.spacing.sm)) {
                FilterChip(
                    selected = filter.categoryId == null,
                    onClick = { onCategoryChanged(null) },
                    label = { Text(stringResource(R.string.history_any_category)) },
                )
                categories.forEach { category ->
                    FilterChip(
                        selected = filter.categoryId == category.id,
                        onClick = { onCategoryChanged(category.id) },
                        label = { Text(category.displayName(resources)) },
                    )
                }
            }
            SheetTitle(stringResource(R.string.history_filter_mode))
            ModeChips(filter.mode, onModeChanged)
            TextButton(onClick = onReset, modifier = Modifier.align(Alignment.End)) {
                Text(stringResource(R.string.history_reset))
            }
        }
    }
}

@Composable
internal fun ModeChips(
    mode: GameMode?,
    onModeChanged: (GameMode?) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(modifier, horizontalArrangement = Arrangement.spacedBy(Lumora.spacing.sm)) {
        FilterChip(
            selected = mode == null,
            onClick = { onModeChanged(null) },
            label = { Text(stringResource(R.string.history_mode_all)) },
        )
        FilterChip(
            selected = mode == GameMode.CLASSIC,
            onClick = { onModeChanged(GameMode.CLASSIC) },
            label = { Text(stringResource(R.string.history_mode_classic)) },
        )
        FilterChip(
            selected = mode == GameMode.MARATHON,
            onClick = { onModeChanged(GameMode.MARATHON) },
            label = { Text(stringResource(R.string.history_mode_marathon)) },
        )
    }
}

@Composable
private fun SheetTitle(text: String) {
    Text(text, style = MaterialTheme.typography.titleMedium, modifier = Modifier.semantics { heading() })
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
