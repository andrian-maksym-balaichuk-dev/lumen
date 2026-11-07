package sk.mpage.lumen.ui

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.navigation3.runtime.NavKey
import sk.mpage.lumen.R
import sk.mpage.lumen.ui.navigation.AwardsKey
import sk.mpage.lumen.ui.navigation.HistoryKey
import sk.mpage.lumen.ui.navigation.LobbyKey
import sk.mpage.lumen.ui.navigation.ProfileKey

internal enum class TopLevelDestination(
    val key: NavKey,
    @param:StringRes val labelRes: Int,
    @param:DrawableRes val iconRes: Int,
) {
    PLAY(LobbyKey, R.string.tab_play, R.drawable.ic_tab_play),
    HISTORY(HistoryKey, R.string.tab_history, R.drawable.ic_tab_history),
    AWARDS(AwardsKey, R.string.tab_awards, R.drawable.ic_tab_awards),
    PROFILE(ProfileKey, R.string.tab_profile, R.drawable.ic_tab_profile),
}
