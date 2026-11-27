package sk.mpage.lumen.feature.lobby.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import sk.mpage.lumen.core.designsystem.icon.LumoraIcons
import sk.mpage.lumen.core.designsystem.theme.Lumora
import sk.mpage.lumen.feature.lobby.R

@Composable
internal fun PersonalBestBadge(
    best: Int,
    modifier: Modifier = Modifier,
) {
    val description = stringResource(R.string.lobby_personal_best_description, best)
    Surface(
        modifier = modifier.clearAndSetSemantics { contentDescription = description },
        shape = MaterialTheme.shapes.extraLarge,
        color = MaterialTheme.colorScheme.tertiaryContainer,
        contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = Lumora.spacing.sm, vertical = Lumora.spacing.xs),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Lumora.spacing.xs),
        ) {
            Icon(
                painter = painterResource(LumoraIcons.Flame),
                contentDescription = null,
                tint = Lumora.colors.streakFlame,
                modifier = Modifier.size(Lumora.sizes.xxs),
            )
            Text(stringResource(R.string.lobby_personal_best, best), style = MaterialTheme.typography.labelLarge)
        }
    }
}
