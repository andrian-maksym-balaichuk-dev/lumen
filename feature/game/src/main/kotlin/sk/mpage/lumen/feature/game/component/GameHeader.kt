package sk.mpage.lumen.feature.game.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import sk.mpage.lumen.core.designsystem.component.ProgressPill
import sk.mpage.lumen.core.designsystem.component.TimerRing
import sk.mpage.lumen.core.designsystem.icon.LumoraIcons
import sk.mpage.lumen.core.designsystem.theme.Lumora
import sk.mpage.lumen.core.domain.game.GameRules
import sk.mpage.lumen.feature.game.R

@Composable
internal fun GameHeader(
    position: Int?,
    total: Int?,
    remainingMs: () -> Long,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier.fillMaxWidth()) {
        IconButton(onClick = onClose, modifier = Modifier.align(Alignment.CenterStart)) {
            Icon(painterResource(LumoraIcons.Close), contentDescription = stringResource(R.string.game_close))
        }
        if (position != null) {
            ProgressPill(
                progress = if (total != null) position.toFloat() / total else 1f,
                label =
                    if (total != null) {
                        stringResource(R.string.game_progress, position, total)
                    } else {
                        stringResource(R.string.game_progress_marathon, position)
                    },
                description =
                    if (total != null) {
                        stringResource(R.string.game_progress_description, position, total)
                    } else {
                        stringResource(R.string.game_progress_marathon_description, position)
                    },
                modifier = Modifier.align(Alignment.Center),
            )
            TimerRing(
                remainingMs = remainingMs,
                totalMs = GameRules.QUESTION_TIME_LIMIT_MS,
                warningAnnouncement = pluralStringResource(R.plurals.game_time_left, WARNING_SECONDS, WARNING_SECONDS),
                modifier = Modifier.align(Alignment.CenterEnd).padding(end = Lumora.spacing.sm),
            )
        }
    }
}

private const val WARNING_SECONDS = 5
