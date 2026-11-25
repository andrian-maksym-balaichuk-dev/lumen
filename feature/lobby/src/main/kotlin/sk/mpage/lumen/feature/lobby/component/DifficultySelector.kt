package sk.mpage.lumen.feature.lobby.component

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import sk.mpage.lumen.core.designsystem.component.LumoraSegmented
import sk.mpage.lumen.core.model.Difficulty
import sk.mpage.lumen.feature.lobby.R

@Composable
internal fun DifficultySelector(
    difficulty: Difficulty,
    onSelect: (Difficulty) -> Unit,
    modifier: Modifier = Modifier,
) {
    LobbySection(title = stringResource(R.string.lobby_difficulty), modifier = modifier) {
        LumoraSegmented(
            options = Difficulty.entries,
            selected = difficulty,
            onSelect = onSelect,
            label = {
                when (it) {
                    Difficulty.ANY -> stringResource(R.string.lobby_difficulty_any)
                    Difficulty.EASY -> stringResource(R.string.lobby_difficulty_easy)
                    Difficulty.MEDIUM -> stringResource(R.string.lobby_difficulty_medium)
                    Difficulty.HARD -> stringResource(R.string.lobby_difficulty_hard)
                }
            },
        )
    }
}
