package sk.mpage.lumen.feature.profile.component

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import sk.mpage.lumen.core.designsystem.component.AvatarGrid
import sk.mpage.lumen.core.designsystem.component.AvatarImage
import sk.mpage.lumen.core.designsystem.icon.LumoraAvatars
import sk.mpage.lumen.core.designsystem.theme.Lumora

@Composable
internal fun AvatarPicker(
    selectedKey: String,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val motion = Lumora.motion
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(Lumora.spacing.lg),
    ) {
        AnimatedContent(
            targetState = LumoraAvatars.preset(selectedKey) ?: LumoraAvatars.Default,
            transitionSpec = {
                (fadeIn(motion.colorChange()) + scaleIn(if (motion.isReduced) spring() else motion.badgePop(), 0.8f))
                    .togetherWith(fadeOut(motion.colorChange()))
            },
            label = "selected avatar",
        ) { preset ->
            AvatarImage(
                presetRes = preset.drawableRes,
                contentDescription = stringResource(preset.nameRes),
                size = 112.dp,
            )
        }
        AvatarGrid(selectedKey = selectedKey, onSelect = onSelect)
    }
}
