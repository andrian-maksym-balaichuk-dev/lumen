package sk.mpage.lumen.core.ui

import android.content.res.Resources
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.res.stringResource

@Immutable
public sealed interface UiText {
    public data class Resource(
        @param:StringRes val id: Int,
    ) : UiText

    public data class Raw(
        val value: String,
    ) : UiText

    @Composable
    public fun asString(): String =
        when (this) {
            is Resource -> stringResource(id)
            is Raw -> value
        }

    public fun asString(resources: Resources): String =
        when (this) {
            is Resource -> resources.getString(id)
            is Raw -> value
        }
}
