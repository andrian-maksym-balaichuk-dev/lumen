package sk.mpage.lumen.core.model

public enum class ThemeMode(
    public val code: String,
) {
    SYSTEM("system"),
    LIGHT("light"),
    DARK("dark"),
    ;

    public companion object {
        public fun fromCode(code: String): ThemeMode =
            requireNotNull(entries.firstOrNull { it.code == code }) { "Unknown theme mode code: $code" }
    }
}
