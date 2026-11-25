package sk.mpage.lumen.feature.lobby.component

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import sk.mpage.lumen.core.designsystem.component.LumoraSegmented
import sk.mpage.lumen.core.model.GameMode
import sk.mpage.lumen.feature.lobby.R

@Composable
internal fun ModeSelector(
    mode: GameMode,
    onSelect: (GameMode) -> Unit,
    modifier: Modifier = Modifier,
) {
    LobbySection(
        title = stringResource(R.string.lobby_mode),
        supportingText =
            when (mode) {
                GameMode.CLASSIC -> stringResource(R.string.lobby_mode_classic_info)
                GameMode.MARATHON -> stringResource(R.string.lobby_mode_marathon_info)
            },
        modifier = modifier,
    ) {
        LumoraSegmented(
            options = GameMode.entries,
            selected = mode,
            onSelect = onSelect,
            label = {
                when (it) {
                    GameMode.CLASSIC -> stringResource(R.string.lobby_mode_classic)
                    GameMode.MARATHON -> stringResource(R.string.lobby_mode_marathon)
                }
            },
        )
    }
}
