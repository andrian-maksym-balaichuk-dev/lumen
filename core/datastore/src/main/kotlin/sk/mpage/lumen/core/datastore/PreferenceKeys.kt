package sk.mpage.lumen.core.datastore

import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey

public object PreferenceKeys {
    public val PROFILE_NAME: Preferences.Key<String> = stringPreferencesKey("profile_name")
    public val PROFILE_AVATAR: Preferences.Key<String> = stringPreferencesKey("profile_avatar")
    public val ONBOARDED: Preferences.Key<Boolean> = booleanPreferencesKey("onboarded")
    public val THEME_MODE: Preferences.Key<String> = stringPreferencesKey("theme_mode")
    public val DYNAMIC_COLOR: Preferences.Key<Boolean> = booleanPreferencesKey("dynamic_color")
    public val REDUCE_MOTION: Preferences.Key<String> = stringPreferencesKey("reduce_motion")
    public val REDUCE_TRANSPARENCY: Preferences.Key<Boolean> =
        booleanPreferencesKey("reduce_transparency")
    public val NOTIF_STREAK: Preferences.Key<Boolean> = booleanPreferencesKey("notif_streak")
    public val NOTIF_INACTIVITY: Preferences.Key<Boolean> =
        booleanPreferencesKey("notif_inactivity")
    public val LAST_QUIZ_CONFIG: Preferences.Key<String> =
        stringPreferencesKey("last_quiz_config")
    public val SESSION_TOKEN: Preferences.Key<String> = stringPreferencesKey("session_token")
    public val SESSION_TOKEN_AT: Preferences.Key<Long> = longPreferencesKey("session_token_at")
}
