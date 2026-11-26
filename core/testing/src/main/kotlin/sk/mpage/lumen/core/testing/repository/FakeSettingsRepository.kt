package sk.mpage.lumen.core.testing.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import sk.mpage.lumen.core.domain.repository.SettingsRepository
import sk.mpage.lumen.core.model.AppSettings
import sk.mpage.lumen.core.model.QuizConfig
import sk.mpage.lumen.core.model.ThemeMode

public class FakeSettingsRepository(
    initial: AppSettings = AppSettings(),
) : SettingsRepository {
    private val settings = MutableStateFlow(initial)

    public val current: AppSettings get() = settings.value

    override fun observeSettings(): Flow<AppSettings> = settings

    override suspend fun setThemeMode(mode: ThemeMode) {
        settings.update { it.copy(themeMode = mode) }
    }

    override suspend fun setDynamicColor(enabled: Boolean) {
        settings.update { it.copy(dynamicColor = enabled) }
    }

    override suspend fun setReduceMotion(value: Boolean?) {
        settings.update { it.copy(reduceMotion = value) }
    }

    override suspend fun setReduceTransparency(enabled: Boolean) {
        settings.update { it.copy(reduceTransparency = enabled) }
    }

    override suspend fun setStreakReminders(enabled: Boolean) {
        settings.update { it.copy(streakReminders = enabled) }
    }

    override suspend fun setInactivityReminders(enabled: Boolean) {
        settings.update { it.copy(inactivityReminders = enabled) }
    }

    override suspend fun setLastQuizConfig(config: QuizConfig) {
        settings.update { it.copy(lastConfig = config) }
    }
}
