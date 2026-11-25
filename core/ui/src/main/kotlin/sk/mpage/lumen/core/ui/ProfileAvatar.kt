package sk.mpage.lumen.core.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import sk.mpage.lumen.core.designsystem.component.AvatarImage
import sk.mpage.lumen.core.designsystem.icon.LumoraAvatars
import sk.mpage.lumen.core.model.Avatar
import java.io.File

@Composable
public fun ProfileAvatar(
    avatar: Avatar,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    size: Dp = 48.dp,
) {
    val context = LocalContext.current
    when (avatar) {
        is Avatar.Custom -> {
            val file =
                remember(avatar.fileName) { File(File(context.filesDir, Avatar.Custom.DIRECTORY), avatar.fileName) }
            AvatarImage(
                presetRes = null,
                file = file,
                contentDescription = contentDescription,
                modifier = modifier,
                size = size,
            )
        }

        is Avatar.Preset -> {
            AvatarImage(
                presetRes = LumoraAvatars.preset(avatar.key)?.drawableRes,
                contentDescription = contentDescription,
                modifier = modifier,
                size = size,
            )
        }

        Avatar.None -> {
            AvatarImage(presetRes = null, contentDescription = contentDescription, modifier = modifier, size = size)
        }
    }
}
