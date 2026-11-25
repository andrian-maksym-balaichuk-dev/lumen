package sk.mpage.lumen.core.designsystem.icon

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.runtime.Immutable
import sk.mpage.lumen.core.designsystem.R

@Immutable
public data class AvatarPreset(
    val key: String,
    @param:DrawableRes val drawableRes: Int,
    @param:StringRes val nameRes: Int,
)

public object LumoraAvatars {
    public val presets: List<AvatarPreset> =
        listOf(
            AvatarPreset("fox", R.drawable.avatar_fox, R.string.avatar_fox),
            AvatarPreset("owl", R.drawable.avatar_owl, R.string.avatar_owl),
            AvatarPreset("cat", R.drawable.avatar_cat, R.string.avatar_cat),
            AvatarPreset("bear", R.drawable.avatar_bear, R.string.avatar_bear),
            AvatarPreset("panda", R.drawable.avatar_panda, R.string.avatar_panda),
            AvatarPreset("frog", R.drawable.avatar_frog, R.string.avatar_frog),
            AvatarPreset("rabbit", R.drawable.avatar_rabbit, R.string.avatar_rabbit),
            AvatarPreset("penguin", R.drawable.avatar_penguin, R.string.avatar_penguin),
        )

    public val Default: AvatarPreset = presets.first()

    public fun preset(key: String): AvatarPreset? = presets.firstOrNull { it.key == key }
}
