package sk.mpage.lumen.core.designsystem.component

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import sk.mpage.lumen.core.designsystem.icon.LumoraAvatars
import sk.mpage.lumen.core.designsystem.icon.LumoraIcons
import sk.mpage.lumen.core.designsystem.preview.PreviewSurface
import java.io.File

@Composable
public fun AvatarImage(
    @DrawableRes presetRes: Int?,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    size: Dp = 48.dp,
    file: File? = null,
) {
    val circle = modifier.size(size).clip(CircleShape)
    if (file != null) {
        AsyncImage(
            model = file,
            contentDescription = contentDescription,
            modifier = circle.background(MaterialTheme.colorScheme.secondaryContainer),
            contentScale = ContentScale.Crop,
        )
    } else if (presetRes != null) {
        Image(
            painter = painterResource(presetRes),
            contentDescription = contentDescription,
            modifier = circle,
            contentScale = ContentScale.Crop,
        )
    } else {
        Box(circle.background(MaterialTheme.colorScheme.secondaryContainer), contentAlignment = Alignment.Center) {
            Icon(
                painter = painterResource(LumoraIcons.Profile),
                contentDescription = contentDescription,
                modifier = Modifier.size(size / 2),
                tint = MaterialTheme.colorScheme.onSecondaryContainer,
            )
        }
    }
}

@PreviewLightDark
@Composable
private fun AvatarImagePreview() {
    PreviewSurface {
        Row(Modifier.padding(16.dp)) {
            AvatarImage(presetRes = null, contentDescription = null, size = 64.dp)
            LumoraAvatars.presets.take(3).forEach {
                AvatarImage(
                    it.drawableRes,
                    contentDescription = null,
                    size = 64.dp,
                    modifier = Modifier.padding(start = 8.dp),
                )
            }
        }
    }
}
