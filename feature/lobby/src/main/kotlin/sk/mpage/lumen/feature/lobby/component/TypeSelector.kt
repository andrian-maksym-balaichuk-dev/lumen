package sk.mpage.lumen.feature.lobby.component

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import sk.mpage.lumen.core.designsystem.component.LumoraSegmented
import sk.mpage.lumen.core.model.QuestionType
import sk.mpage.lumen.feature.lobby.R

@Composable
internal fun TypeSelector(
    type: QuestionType,
    onSelect: (QuestionType) -> Unit,
    modifier: Modifier = Modifier,
) {
    LobbySection(title = stringResource(R.string.lobby_type), modifier = modifier) {
        LumoraSegmented(
            options = QuestionType.entries,
            selected = type,
            onSelect = onSelect,
            label = {
                when (it) {
                    QuestionType.MULTIPLE_CHOICE -> stringResource(R.string.lobby_type_multiple)
                    QuestionType.TRUE_FALSE -> stringResource(R.string.lobby_type_boolean)
                }
            },
        )
    }
}
