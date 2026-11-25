package sk.mpage.lumen.core.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import sk.mpage.lumen.core.datastore.PreferenceKeys
import sk.mpage.lumen.core.datastore.UserPreferencesDataSource
import sk.mpage.lumen.core.domain.repository.SettingsRepository
import sk.mpage.lumen.core.model.AppSettings
import sk.mpage.lumen.core.model.QuizConfig
import sk.mpage.lumen.core.model.ThemeMode
import javax.inject.Inject

internal class DataStoreSettingsRepository
    @Inject
    constructor(
        private val preferences: UserPreferencesDataSource,
    ) : SettingsRepository {
        private val defaults = AppSettings()

        override fun observeSettings(): Flow<AppSettings> =
            preferences.preferences.map { prefs ->
                AppSettings(
                    themeMode = prefs[PreferenceKeys.THEME_MODE]?.let(::themeModeOrNull) ?: defaults.themeMode,
                    dynamicColor = prefs[PreferenceKeys.DYNAMIC_COLOR] ?: defaults.dynamicColor,
                    reduceMotion = prefs[PreferenceKeys.REDUCE_MOTION]?.let(::reduceMotionFromCode) ?: defaults.reduceMotion,
                    reduceTransparency = prefs[PreferenceKeys.REDUCE_TRANSPARENCY] ?: defaults.reduceTransparency,
                    streakReminders = prefs[PreferenceKeys.NOTIF_STREAK] ?: defaults.streakReminders,
                    inactivityReminders = prefs[PreferenceKeys.NOTIF_INACTIVITY] ?: defaults.inactivityReminders,
                    lastConfig = prefs[PreferenceKeys.LAST_QUIZ_CONFIG]?.let(::quizConfigOrNull),
                )
            }

        override suspend fun setThemeMode(mode: ThemeMode) = preferences.setThemeMode(mode.code)

        override suspend fun setDynamicColor(enabled: Boolean) = preferences.setDynamicColor(enabled)

        override suspend fun setReduceMotion(value: Boolean?) = preferences.setReduceMotion(reduceMotionToCode(value))

        override suspend fun setReduceTransparency(enabled: Boolean) = preferences.setReduceTransparency(enabled)

        override suspend fun setStreakReminders(enabled: Boolean) = preferences.setStreakReminders(enabled)

        override suspend fun setInactivityReminders(enabled: Boolean) = preferences.setInactivityReminders(enabled)

        override suspend fun setLastQuizConfig(config: QuizConfig) =
            preferences.setLastQuizConfig(Json.encodeToString(QuizConfig.serializer(), config))

        private fun themeModeOrNull(code: String): ThemeMode? = ThemeMode.entries.firstOrNull { it.code == code }

        private fun quizConfigOrNull(json: String): QuizConfig? =
            try {
                Json.decodeFromString(QuizConfig.serializer(), json)
            } catch (_: SerializationException) {
                null
            } catch (_: IllegalArgumentException) {
                null
            }

        private fun reduceMotionFromCode(code: String): Boolean? =
            when (code) {
                REDUCE_MOTION_ON -> true
                REDUCE_MOTION_OFF -> false
                else -> null
            }

        private fun reduceMotionToCode(value: Boolean?): String =
            when (value) {
                true -> REDUCE_MOTION_ON
                false -> REDUCE_MOTION_OFF
                null -> REDUCE_MOTION_SYSTEM
            }

        private companion object {
            const val REDUCE_MOTION_SYSTEM = "system"
            const val REDUCE_MOTION_ON = "on"
            const val REDUCE_MOTION_OFF = "off"
        }
    }
