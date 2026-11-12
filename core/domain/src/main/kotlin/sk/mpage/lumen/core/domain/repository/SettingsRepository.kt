package sk.mpage.lumen.core.domain.repository

import kotlinx.coroutines.flow.Flow
import sk.mpage.lumen.core.model.AppSettings
import sk.mpage.lumen.core.model.QuizConfig
import sk.mpage.lumen.core.model.ThemeMode

public interface SettingsRepository {
    public fun observeSettings(): Flow<AppSettings>

    public suspend fun setThemeMode(mode: ThemeMode)

    public suspend fun setDynamicColor(enabled: Boolean)

    public suspend fun setReduceMotion(value: Boolean?)

    public suspend fun setReduceTransparency(enabled: Boolean)

    public suspend fun setStreakReminders(enabled: Boolean)

    public suspend fun setInactivityReminders(enabled: Boolean)

    public suspend fun setLastQuizConfig(config: QuizConfig)
}
