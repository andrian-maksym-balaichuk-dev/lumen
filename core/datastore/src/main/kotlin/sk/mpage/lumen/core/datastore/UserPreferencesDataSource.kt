package sk.mpage.lumen.core.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import java.io.IOException
import javax.inject.Inject

public class UserPreferencesDataSource
    @Inject
    constructor(
        private val dataStore: DataStore<Preferences>,
    ) {
        public val preferences: Flow<Preferences> =
            dataStore.data.catch { e -> if (e is IOException) emit(emptyPreferences()) else throw e }

        public suspend fun setProfile(
            name: String,
            avatar: String,
        ) {
            dataStore.edit {
                it[PreferenceKeys.PROFILE_NAME] = name
                it[PreferenceKeys.PROFILE_AVATAR] = avatar
                it[PreferenceKeys.ONBOARDED] = true
            }
        }

        public suspend fun setProfileName(name: String) {
            dataStore.edit { it[PreferenceKeys.PROFILE_NAME] = name }
        }

        public suspend fun setProfileAvatar(avatar: String) {
            dataStore.edit { it[PreferenceKeys.PROFILE_AVATAR] = avatar }
        }

        public suspend fun setThemeMode(code: String) {
            dataStore.edit { it[PreferenceKeys.THEME_MODE] = code }
        }

        public suspend fun setDynamicColor(enabled: Boolean) {
            dataStore.edit { it[PreferenceKeys.DYNAMIC_COLOR] = enabled }
        }

        public suspend fun setReduceMotion(code: String) {
            dataStore.edit { it[PreferenceKeys.REDUCE_MOTION] = code }
        }

        public suspend fun setReduceTransparency(enabled: Boolean) {
            dataStore.edit { it[PreferenceKeys.REDUCE_TRANSPARENCY] = enabled }
        }

        public suspend fun setStreakReminders(enabled: Boolean) {
            dataStore.edit { it[PreferenceKeys.NOTIF_STREAK] = enabled }
        }

        public suspend fun setInactivityReminders(enabled: Boolean) {
            dataStore.edit { it[PreferenceKeys.NOTIF_INACTIVITY] = enabled }
        }

        public suspend fun setLastQuizConfig(json: String) {
            dataStore.edit { it[PreferenceKeys.LAST_QUIZ_CONFIG] = json }
        }

        public suspend fun setSessionToken(
            token: String,
            issuedAtEpochMs: Long,
        ) {
            dataStore.edit {
                it[PreferenceKeys.SESSION_TOKEN] = token
                it[PreferenceKeys.SESSION_TOKEN_AT] = issuedAtEpochMs
            }
        }

        public suspend fun markActive(epochMs: Long) {
            dataStore.edit {
                it[PreferenceKeys.LAST_ACTIVE_AT] = epochMs
                if (PreferenceKeys.FIRST_LAUNCHED_AT !in it) it[PreferenceKeys.FIRST_LAUNCHED_AT] = epochMs
            }
        }

        public suspend fun clearSessionToken() {
            dataStore.edit {
                it.remove(PreferenceKeys.SESSION_TOKEN)
                it.remove(PreferenceKeys.SESSION_TOKEN_AT)
            }
        }
    }
