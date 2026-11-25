package sk.mpage.lumen.feature.profile.component

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import sk.mpage.lumen.core.designsystem.component.AvatarGrid
import sk.mpage.lumen.core.designsystem.icon.LumoraAvatars
import sk.mpage.lumen.core.designsystem.theme.Lumora
import sk.mpage.lumen.core.model.Avatar
import sk.mpage.lumen.core.ui.ProfileAvatar
import sk.mpage.lumen.feature.profile.R

@Composable
internal fun AvatarPicker(
    avatar: Avatar,
    isImportingPhoto: Boolean,
    onPresetSelected: (String) -> Unit,
    onPhotoPicked: (String) -> Unit,
    modifier: Modifier = Modifier,
    previewSize: Dp = 112.dp,
) {
    val motion = Lumora.motion
    val photoPicker =
        rememberLauncherForActivityResult(PickVisualMedia()) { uri ->
            if (uri != null) onPhotoPicked(uri.toString())
        }
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(Lumora.spacing.lg),
    ) {
        Box(contentAlignment = Alignment.Center) {
            AnimatedContent(
                targetState = avatar,
                transitionSpec = {
                    (
                        fadeIn(
                            motion.colorChange(),
                        ) + scaleIn(if (motion.isReduced) spring() else motion.badgePop(), 0.8f)
                    ).togetherWith(fadeOut(motion.colorChange()))
                },
                label = "selected avatar",
            ) { shown ->
                ProfileAvatar(avatar = shown, contentDescription = avatarDescription(shown), size = previewSize)
            }
            if (isImportingPhoto) CircularProgressIndicator(Modifier.size(48.dp))
        }
        AvatarGrid(selectedKey = (avatar as? Avatar.Preset)?.key, onSelect = onPresetSelected)
        FilledTonalButton(
            onClick = { photoPicker.launch(PickVisualMediaRequest(PickVisualMedia.ImageOnly)) },
            enabled = !isImportingPhoto,
        ) {
            Text(stringResource(R.string.profile_choose_photo))
        }
    }
}

@Composable
private fun avatarDescription(avatar: Avatar): String? =
    when (avatar) {
        is Avatar.Preset -> LumoraAvatars.preset(avatar.key)?.let { stringResource(it.nameRes) }
        is Avatar.Custom -> stringResource(R.string.profile_avatar_photo)
        Avatar.None -> null
    }
