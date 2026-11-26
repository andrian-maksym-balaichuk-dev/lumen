package sk.mpage.lumen.feature.lobby.component

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.stringResource
import sk.mpage.lumen.core.common.result.DataError
import sk.mpage.lumen.core.designsystem.component.CategoryChipGrid
import sk.mpage.lumen.core.designsystem.component.CategoryChipPlaceholders
import sk.mpage.lumen.core.designsystem.component.ErrorState
import sk.mpage.lumen.core.designsystem.icon.LumoraIcons
import sk.mpage.lumen.core.ui.categoryIcon
import sk.mpage.lumen.core.ui.displayName
import sk.mpage.lumen.core.ui.toUiText
import sk.mpage.lumen.feature.lobby.CategoriesUiState
import sk.mpage.lumen.feature.lobby.R
import java.text.Collator

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
                val resources = LocalResources.current
                val locale = LocalConfiguration.current.locales[0]
                val names =
                    remember(categories, resources) {
                        categories.categories.associate { it.id to it.displayName(resources) }
                    }
                val options =
                    remember(names, locale) {
                        val collator = Collator.getInstance(locale)
                        listOf<Int?>(null) + names.entries.sortedWith(compareBy(collator) { it.value }).map { it.key }
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
