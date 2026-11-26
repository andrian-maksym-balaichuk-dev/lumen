package sk.mpage.lumen.feature.lobby.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.PreviewLightDark
import sk.mpage.lumen.core.designsystem.icon.LumoraIcons
import sk.mpage.lumen.core.designsystem.preview.PreviewSurface
import sk.mpage.lumen.core.designsystem.theme.Lumora
import sk.mpage.lumen.feature.lobby.R

@Composable
internal fun OfflineBanner(
    visible: Boolean,
    modifier: Modifier = Modifier,
) {
    val motion = Lumora.motion
    AnimatedVisibility(
        visible = visible,
        modifier = modifier,
        enter = expandVertically(motion.screen()) + fadeIn(motion.colorChange()),
        exit = shrinkVertically(motion.screen()) + fadeOut(motion.colorChange()),
    ) {
        Surface(
            modifier =
                Modifier
                    .widthIn(max = Lumora.spacing.contentMaxWidth)
                    .fillMaxWidth()
                    .padding(bottom = Lumora.spacing.sm)
                    .semantics { liveRegion = LiveRegionMode.Polite },
            shape = MaterialTheme.shapes.medium,
            color = MaterialTheme.colorScheme.inverseSurface,
            contentColor = MaterialTheme.colorScheme.inverseOnSurface,
        ) {
            Row(
                modifier = Modifier.padding(horizontal = Lumora.spacing.lg, vertical = Lumora.spacing.md),
                horizontalArrangement = Arrangement.spacedBy(Lumora.spacing.md),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    painter = painterResource(LumoraIcons.CloudOff),
                    contentDescription = null,
                    modifier = Modifier.size(Lumora.sizes.xs),
                )
                Column {
                    Text(stringResource(R.string.lobby_offline_title), style = MaterialTheme.typography.titleSmall)
                    Text(stringResource(R.string.lobby_offline_body), style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}

@PreviewLightDark
@Composable
private fun OfflineBannerPreview() {
    PreviewSurface {
        OfflineBanner(visible = true, modifier = Modifier.padding(Lumora.spacing.lg))
    }
}
