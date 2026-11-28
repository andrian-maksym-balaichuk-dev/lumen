package sk.mpage.lumen.ui

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.navigation3.runtime.NavKey
import sk.mpage.lumen.R
import sk.mpage.lumen.core.designsystem.icon.LumoraIcons
import sk.mpage.lumen.feature.awards.navigation.AwardsKey
import sk.mpage.lumen.feature.history.navigation.HistoryKey
import sk.mpage.lumen.feature.lobby.navigation.LobbyKey
import sk.mpage.lumen.feature.profile.navigation.ProfileKey

internal enum class TopLevelDestination(
    val key: NavKey,
    @param:StringRes val labelRes: Int,
    @param:DrawableRes val iconRes: Int,
) {
    PLAY(LobbyKey, R.string.tab_play, LumoraIcons.Play),
    HISTORY(HistoryKey, R.string.tab_history, LumoraIcons.History),
    AWARDS(AwardsKey, R.string.tab_awards, LumoraIcons.Awards),
    PROFILE(ProfileKey, R.string.tab_profile, LumoraIcons.Profile),
}
