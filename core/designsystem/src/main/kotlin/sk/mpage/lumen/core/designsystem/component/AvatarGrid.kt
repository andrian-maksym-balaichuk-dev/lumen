package sk.mpage.lumen.core.designsystem.component

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import sk.mpage.lumen.core.designsystem.R
import sk.mpage.lumen.core.designsystem.icon.AvatarPreset
import sk.mpage.lumen.core.designsystem.icon.LumoraAvatars
import sk.mpage.lumen.core.designsystem.preview.PreviewSurface
import sk.mpage.lumen.core.designsystem.theme.Lumora

@Composable
public fun AvatarGrid(
    selectedKey: String?,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val presets = LumoraAvatars.presets
    FlowRow(
        modifier = modifier.fillMaxWidth().selectableGroup(),
        horizontalArrangement = Arrangement.spacedBy(Lumora.spacing.sm, Alignment.CenterHorizontally),
        verticalArrangement = Arrangement.spacedBy(Lumora.spacing.sm),
    ) {
        presets.forEachIndexed { index, preset ->
            AvatarChoice(
                preset = preset,
                description =
                    stringResource(
                        R.string.avatar_preset_description,
                        index + 1,
                        presets.size,
                        stringResource(preset.nameRes),
                    ),
                selected = preset.key == selectedKey,
                onClick = { onSelect(preset.key) },
            )
        }
    }
}

@Composable
private fun AvatarChoice(
    preset: AvatarPreset,
    description: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val motion = Lumora.motion
    val ring by animateDpAsState(if (selected) RING_WIDTH else 0.dp, motion.selection(), label = "avatar ring")
    val scale by animateFloatAsState(if (selected) 1f else 0.9f, motion.selection(), label = "avatar scale")
    Box(
        modifier =
            Modifier
                .size(Lumora.sizes.xl)
                .clip(CircleShape)
                .selectable(selected = selected, role = Role.RadioButton, onClick = onClick)
                .semantics { contentDescription = description },
        contentAlignment = Alignment.Center,
    ) {
        AvatarImage(
            presetRes = preset.drawableRes,
            contentDescription = null,
            size = Lumora.sizes.lg,
            modifier =
                Modifier
                    .scale(scale)
                    .border(
                        ring.coerceAtLeast(0.dp),
                        if (selected) MaterialTheme.colorScheme.primary else Color.Transparent,
                        CircleShape,
                    ).padding(RING_WIDTH),
        )
    }
}

private val RING_WIDTH = 3.dp

@PreviewLightDark
@Composable
private fun AvatarGridPreview() {
    PreviewSurface {
        AvatarGrid(selectedKey = "cat", onSelect = {}, modifier = Modifier.padding(Lumora.spacing.lg))
    }
}
