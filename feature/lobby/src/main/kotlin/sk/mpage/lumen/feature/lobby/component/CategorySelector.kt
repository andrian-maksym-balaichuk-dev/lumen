package sk.mpage.lumen.feature.lobby.component

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import sk.mpage.lumen.core.common.result.DataError
import sk.mpage.lumen.core.designsystem.component.CategoryChipGrid
import sk.mpage.lumen.core.designsystem.component.CategoryChipPlaceholders
import sk.mpage.lumen.core.designsystem.component.ErrorState
import sk.mpage.lumen.core.designsystem.icon.LumoraIcons
import sk.mpage.lumen.core.ui.categoryIcon
import sk.mpage.lumen.core.ui.shortName
import sk.mpage.lumen.core.ui.toUiText
import sk.mpage.lumen.feature.lobby.CategoriesUiState
import sk.mpage.lumen.feature.lobby.R

@Composable
internal fun CategorySelector(
    categories: CategoriesUiState,
    categoryId: Int?,
    onSelect: (Int?) -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    LobbySection(title = stringResource(R.string.lobby_category), modifier = modifier) {
        when (categories) {
            CategoriesUiState.Loading -> {
                CategoryChipPlaceholders()
            }

            is CategoriesUiState.Failed -> {
                ErrorState(
                    title = stringResource(R.string.lobby_categories_failed),
                    message = categories.error.toUiText().asString(),
                    retryLabel = stringResource(R.string.lobby_retry),
                    onRetry = onRetry,
                    icon = if (categories.error == DataError.NoInternet) LumoraIcons.CloudOff else LumoraIcons.Error,
                )
            }

            is CategoriesUiState.Loaded -> {
                val anyLabel = stringResource(R.string.lobby_category_any)
                val names = remember(categories) { categories.categories.associate { it.id to it.shortName } }
                val options =
                    remember(categories) {
                        listOf<Int?>(null) + categories.categories.sortedBy { it.shortName }.map { it.id }
                    }
                CategoryChipGrid(
                    options = options,
                    selected = categoryId,
                    onSelect = onSelect,
                    label = { id -> if (id == null) anyLabel else names.getValue(id) },
                    icon = ::categoryIcon,
                )
            }
        }
    }
}
