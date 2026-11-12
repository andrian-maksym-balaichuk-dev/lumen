package sk.mpage.lumen.core.model

public data class AppSettings(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val dynamicColor: Boolean = false,
    val reduceMotion: Boolean? = null,
    val reduceTransparency: Boolean = false,
    val streakReminders: Boolean = true,
    val inactivityReminders: Boolean = true,
    val lastConfig: QuizConfig? = null,
)
